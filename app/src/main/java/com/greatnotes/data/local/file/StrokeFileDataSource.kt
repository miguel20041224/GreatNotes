package com.greatnotes.data.local.file

import com.greatnotes.domain.model.Page
import com.greatnotes.domain.model.Stroke
import com.greatnotes.domain.model.StrokePoint
import com.greatnotes.domain.model.ToolType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** The stroke file is damaged, truncated or not a stroke file at all. */
class CorruptStrokeFileException(message: String, cause: Throwable? = null) : IOException(message, cause)

/** The stroke file was written by a newer (or unknown) format version. */
class UnsupportedStrokeFormatException(val version: Int) :
    IOException("Unsupported stroke file format version: $version")

/**
 * Persists the strokes of each page as one binary file: `{strokesDir}/{pageId}.strokes`.
 *
 * The directory is injected (production: `context.filesDir/strokes`) so this class has no
 * Android dependency and can be unit-tested on the JVM. All operations are `suspend` and run
 * on the injected [ioDispatcher].
 *
 * ## Binary layout (format version 1, big-endian)
 *
 * ```
 * Header (12 bytes)
 *   magic        4 bytes   0x474E5354 ("GNST")
 *   version      u16       format version, currently 1
 *   flags        u16       reserved, written as 0, ignored on read
 *   strokeCount  i32       number of stroke records that follow
 *
 * Stroke record (repeated strokeCount times)
 *   idLength     u16       length in bytes of the UTF-8 id
 *   id           bytes     UTF-8 stroke id
 *   toolType     u8        0 = PEN, 1 = HIGHLIGHTER, 2 = ERASER_MASK (explicit codes, not ordinals)
 *   colorArgb    i32
 *   baseWidth    f32
 *   pointCount   i32
 *   points       pointCount * 24 bytes
 *
 * Point record (24 bytes)
 *   x f32, y f32, pressure f32, tilt f32 (NaN = no tilt reported), timestampMs i64
 * ```
 *
 * The file must end exactly after the last point; trailing bytes are treated as corruption.
 * A null `tilt` is stored as NaN, so a non-null NaN tilt reads back as null.
 *
 * ## Evolving the format
 * Never change the meaning of an existing version. Bump `FORMAT_VERSION`, add a reader for the
 * new version and keep the old readers so existing files keep loading (the writer always emits
 * the newest version). Files with an unknown version fail with [UnsupportedStrokeFormatException]
 * instead of being misparsed.
 *
 * ## Durability
 * Writes go to `{pageId}.strokes.tmp`, are fsync'ed and then atomically renamed over the target,
 * so a crash mid-save leaves either the old or the new complete file, never a partial one.
 * Writes and deletes are serialized by a mutex; reads need no lock thanks to the atomic rename.
 */
class StrokeFileDataSource(
    private val strokesDir: File,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val writeMutex = Mutex()

    /** File name for a page, validated to prevent path traversal. Equals [Page.strokeFileNameFor]. */
    fun fileNameFor(pageId: String): String {
        require(PAGE_ID_REGEX.matches(pageId)) { "Invalid page id: $pageId" }
        return Page.strokeFileNameFor(pageId)
    }

    /** Atomically replaces the strokes of [pageId]. */
    suspend fun write(pageId: String, strokes: List<Stroke>) {
        val target = File(strokesDir, fileNameFor(pageId))
        withContext(ioDispatcher) {
            val bytes = encode(strokes)
            writeMutex.withLock { atomicWrite(target, bytes) }
        }
    }

    /**
     * Reads the strokes of [pageId]; returns an empty list when the page has no file yet.
     *
     * @throws CorruptStrokeFileException if the file is damaged.
     * @throws UnsupportedStrokeFormatException if the file version is unknown.
     */
    suspend fun read(pageId: String): List<Stroke> {
        val source = File(strokesDir, fileNameFor(pageId))
        return withContext(ioDispatcher) {
            val bytes = try {
                Files.readAllBytes(source.toPath())
            } catch (_: java.nio.file.NoSuchFileException) {
                return@withContext emptyList()
            }
            decode(bytes)
        }
    }

    /** @return true if a file was deleted. */
    suspend fun delete(pageId: String): Boolean {
        val target = File(strokesDir, fileNameFor(pageId))
        return withContext(ioDispatcher) {
            writeMutex.withLock {
                Files.deleteIfExists(File(strokesDir, target.name + TMP_SUFFIX).toPath())
                Files.deleteIfExists(target.toPath())
            }
        }
    }

    private fun atomicWrite(target: File, bytes: ByteArray) {
        if (!strokesDir.isDirectory && !strokesDir.mkdirs() && !strokesDir.isDirectory) {
            throw IOException("Cannot create strokes directory: $strokesDir")
        }
        val tmp = File(strokesDir, target.name + TMP_SUFFIX)
        try {
            FileOutputStream(tmp).use { out ->
                out.write(bytes)
                out.flush()
                out.fd.sync()
            }
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } catch (e: Throwable) {
            tmp.delete()
            throw e
        }
    }

    internal companion object {
        const val MAGIC = 0x474E5354
        const val FORMAT_VERSION = 1
        const val TMP_SUFFIX = ".tmp"

        private const val HEADER_SIZE = 12
        private const val POINT_SIZE = 24
        /** idLength(2) + toolType(1) + color(4) + width(4) + pointCount(4), with an empty id. */
        private const val MIN_STROKE_SIZE = 15
        private const val MAX_ID_BYTES = 0xFFFF

        private const val TOOL_PEN = 0
        private const val TOOL_HIGHLIGHTER = 1
        private const val TOOL_ERASER_MASK = 2

        private val PAGE_ID_REGEX = Regex("^[A-Za-z0-9_-]{1,64}$")

        fun encode(strokes: List<Stroke>): ByteArray {
            val ids = strokes.map { it.id.toByteArray(Charsets.UTF_8) }
            ids.forEach { require(it.size <= MAX_ID_BYTES) { "Stroke id too long" } }
            var size = HEADER_SIZE.toLong()
            strokes.forEachIndexed { i, s ->
                size += MIN_STROKE_SIZE + ids[i].size + s.points.size.toLong() * POINT_SIZE
            }
            require(size <= Int.MAX_VALUE) { "Stroke data too large" }

            val buf = ByteBuffer.allocate(size.toInt()).order(ByteOrder.BIG_ENDIAN)
            buf.putInt(MAGIC)
            buf.putShort(FORMAT_VERSION.toShort())
            buf.putShort(0)
            buf.putInt(strokes.size)
            strokes.forEachIndexed { i, s ->
                buf.putShort(ids[i].size.toShort())
                buf.put(ids[i])
                buf.put(toolCode(s.toolType).toByte())
                buf.putInt(s.colorArgb)
                buf.putFloat(s.baseWidth)
                buf.putInt(s.points.size)
                for (p in s.points) {
                    buf.putFloat(p.x)
                    buf.putFloat(p.y)
                    buf.putFloat(p.pressure)
                    buf.putFloat(p.tilt ?: Float.NaN)
                    buf.putLong(p.timestampMs)
                }
            }
            return buf.array()
        }

        fun decode(bytes: ByteArray): List<Stroke> {
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
            try {
                if (buf.remaining() < HEADER_SIZE) throw CorruptStrokeFileException("File too small")
                if (buf.getInt() != MAGIC) throw CorruptStrokeFileException("Bad magic number")
                val version = buf.getShort().toInt() and 0xFFFF
                buf.getShort() // flags: reserved
                val strokeCount = buf.getInt()
                return when (version) {
                    1 -> readV1(buf, strokeCount)
                    else -> throw UnsupportedStrokeFormatException(version)
                }
            } catch (e: BufferUnderflowException) {
                throw CorruptStrokeFileException("Unexpected end of file", e)
            }
        }

        private fun readV1(buf: ByteBuffer, strokeCount: Int): List<Stroke> {
            if (strokeCount < 0 || strokeCount.toLong() * MIN_STROKE_SIZE > buf.remaining()) {
                throw CorruptStrokeFileException("Invalid stroke count: $strokeCount")
            }
            val strokes = ArrayList<Stroke>(strokeCount)
            repeat(strokeCount) {
                val idLength = buf.getShort().toInt() and 0xFFFF
                if (idLength > buf.remaining()) throw CorruptStrokeFileException("Invalid id length")
                val idBytes = ByteArray(idLength)
                buf.get(idBytes)
                val tool = toolType(buf.get().toInt() and 0xFF)
                val color = buf.getInt()
                val width = buf.getFloat()
                val pointCount = buf.getInt()
                if (pointCount < 0 || pointCount.toLong() * POINT_SIZE > buf.remaining()) {
                    throw CorruptStrokeFileException("Invalid point count: $pointCount")
                }
                val points = ArrayList<StrokePoint>(pointCount)
                repeat(pointCount) {
                    val x = buf.getFloat()
                    val y = buf.getFloat()
                    val pressure = buf.getFloat()
                    val tilt = buf.getFloat()
                    val ts = buf.getLong()
                    points.add(StrokePoint(x, y, pressure, if (tilt.isNaN()) null else tilt, ts))
                }
                strokes.add(Stroke(String(idBytes, Charsets.UTF_8), tool, color, width, points))
            }
            if (buf.hasRemaining()) throw CorruptStrokeFileException("Unexpected trailing data")
            return strokes
        }

        private fun toolCode(tool: ToolType): Int = when (tool) {
            ToolType.PEN -> TOOL_PEN
            ToolType.HIGHLIGHTER -> TOOL_HIGHLIGHTER
            ToolType.ERASER_MASK -> TOOL_ERASER_MASK
        }

        private fun toolType(code: Int): ToolType = when (code) {
            TOOL_PEN -> ToolType.PEN
            TOOL_HIGHLIGHTER -> ToolType.HIGHLIGHTER
            TOOL_ERASER_MASK -> ToolType.ERASER_MASK
            else -> throw CorruptStrokeFileException("Unknown tool type code: $code")
        }
    }
}
