package com.greatnotes.data.local.file

import com.greatnotes.domain.model.Page
import com.greatnotes.domain.model.Stroke
import com.greatnotes.domain.model.StrokePoint
import com.greatnotes.domain.model.ToolType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.ByteBuffer

@OptIn(ExperimentalCoroutinesApi::class)
class StrokeFileDataSourceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var dir: File
    private lateinit var dataSource: StrokeFileDataSource

    private val pageId = "6f1c1b0e-8d52-4a3e-9d0c-2b7c4a1e9f10"

    @Before
    fun setUp() {
        dir = File(tempFolder.root, "strokes")
        dataSource = StrokeFileDataSource(dir, dispatcher)
    }

    private fun sampleStrokes(): List<Stroke> = listOf(
        Stroke(
            id = "stroke-1",
            toolType = ToolType.PEN,
            colorArgb = 0xFF112233.toInt(),
            baseWidth = 2.5f,
            points = listOf(
                StrokePoint(1.5f, -2.25f, 0.5f, 0.75f, 1_000L),
                StrokePoint(Float.MAX_VALUE, Float.MIN_VALUE, 1f, null, Long.MAX_VALUE),
            ),
        ),
        Stroke("stroke-2", ToolType.HIGHLIGHTER, 0x80FFFF00.toInt(), 12f, emptyList()),
        Stroke(
            "stroke-é-3",
            ToolType.ERASER_MASK,
            0,
            20f,
            listOf(StrokePoint(0f, 0f, 0f, null, 0L)),
        ),
    )

    private fun strokeFile() = File(dir, "$pageId.strokes")

    private fun header(magic: Int = StrokeFileDataSource.MAGIC, version: Int = 1, count: Int = 0): ByteArray =
        ByteBuffer.allocate(12).putInt(magic).putShort(version.toShort()).putShort(0).putInt(count).array()

    private fun writeRaw(bytes: ByteArray) {
        dir.mkdirs()
        strokeFile().writeBytes(bytes)
    }

    @Test
    fun roundTrip_returnsIdenticalStrokes() = runTest(dispatcher) {
        val strokes = sampleStrokes()
        dataSource.write(pageId, strokes)
        assertEquals(strokes, dataSource.read(pageId))
    }

    @Test
    fun roundTrip_emptyList() = runTest(dispatcher) {
        dataSource.write(pageId, emptyList())
        assertEquals(emptyList<Stroke>(), dataSource.read(pageId))
    }

    @Test
    fun read_missingFile_returnsEmptyList() = runTest(dispatcher) {
        assertEquals(emptyList<Stroke>(), dataSource.read(pageId))
    }

    @Test
    fun write_replacesPreviousContent_andLeavesNoTmpFile() = runTest(dispatcher) {
        dataSource.write(pageId, sampleStrokes())
        val replacement = sampleStrokes().take(1)
        dataSource.write(pageId, replacement)

        assertEquals(replacement, dataSource.read(pageId))
        assertEquals(listOf("$pageId.strokes"), dir.list()!!.toList())
    }

    @Test
    fun write_startsWithMagicAndVersion() = runTest(dispatcher) {
        dataSource.write(pageId, sampleStrokes())
        val buf = ByteBuffer.wrap(strokeFile().readBytes())
        assertEquals(StrokeFileDataSource.MAGIC, buf.getInt())
        assertEquals(StrokeFileDataSource.FORMAT_VERSION, buf.getShort().toInt())
        buf.getShort()
        assertEquals(3, buf.getInt())
    }

    @Test
    fun delete_removesFile() = runTest(dispatcher) {
        dataSource.write(pageId, sampleStrokes())
        assertTrue(dataSource.delete(pageId))
        assertFalse(strokeFile().exists())
        assertFalse(dataSource.delete(pageId))
    }

    @Test
    fun read_badMagic_throwsCorrupt() = runTest(dispatcher) {
        writeRaw(header(magic = 0x12345678))
        assertThrows(CorruptStrokeFileException::class.java) {
            kotlinx.coroutines.runBlocking { dataSource.read(pageId) }
        }
    }

    @Test
    fun read_unknownVersion_throwsUnsupported() = runTest(dispatcher) {
        writeRaw(header(version = 99))
        val e = assertThrows(UnsupportedStrokeFormatException::class.java) {
            kotlinx.coroutines.runBlocking { dataSource.read(pageId) }
        }
        assertEquals(99, e.version)
    }

    @Test
    fun read_truncatedFile_throwsCorrupt() = runTest(dispatcher) {
        dataSource.write(pageId, sampleStrokes())
        val bytes = strokeFile().readBytes()
        writeRaw(bytes.copyOf(bytes.size - 5))
        assertThrows(CorruptStrokeFileException::class.java) {
            kotlinx.coroutines.runBlocking { dataSource.read(pageId) }
        }
    }

    @Test
    fun read_hugeStrokeCount_throwsCorruptWithoutAllocating() = runTest(dispatcher) {
        writeRaw(header(count = Int.MAX_VALUE))
        assertThrows(CorruptStrokeFileException::class.java) {
            kotlinx.coroutines.runBlocking { dataSource.read(pageId) }
        }
    }

    @Test
    fun read_trailingGarbage_throwsCorrupt() = runTest(dispatcher) {
        dataSource.write(pageId, sampleStrokes())
        writeRaw(strokeFile().readBytes() + byteArrayOf(1, 2, 3))
        assertThrows(CorruptStrokeFileException::class.java) {
            kotlinx.coroutines.runBlocking { dataSource.read(pageId) }
        }
    }

    @Test
    fun invalidPageId_isRejected() {
        listOf("../evil", "a/b", "", "a b").forEach { bad ->
            assertThrows(IllegalArgumentException::class.java) { dataSource.fileNameFor(bad) }
        }
    }

    @Test
    fun fileName_matchesDomainConvention() {
        assertEquals(Page.strokeFileNameFor(pageId), dataSource.fileNameFor(pageId))
    }
}
