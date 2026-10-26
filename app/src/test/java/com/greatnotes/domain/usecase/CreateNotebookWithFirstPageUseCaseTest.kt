package com.greatnotes.domain.usecase

import com.greatnotes.data.local.dao.NotebookDao
import com.greatnotes.data.local.dao.PageDao
import com.greatnotes.data.local.entity.NotebookEntity
import com.greatnotes.data.local.entity.PageEntity
import com.greatnotes.data.local.file.StrokeFileDataSource
import com.greatnotes.data.repository.NotebookRepositoryImpl
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.PageTemplate
import com.greatnotes.domain.util.IdGenerator
import com.greatnotes.domain.util.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import java.io.IOException

/**
 * Exercises the use case through the real [NotebookRepositoryImpl] and a real
 * [StrokeFileDataSource] on a temp dir. Room is replaced by an in-memory fake DAO, so the
 * atomicity of the real @Transaction is NOT covered here (needs an instrumented test).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreateNotebookWithFirstPageUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var strokesDir: File
    private lateinit var dao: FakeNotebookDao
    private lateinit var useCase: CreateNotebookWithFirstPageUseCase

    private var counter = 0
    private val ids = IdGenerator { "00000000-0000-0000-0000-%012d".format(++counter) }
    private val time = TimeProvider { 1_000L }

    @Before
    fun setUp() {
        strokesDir = File(tempFolder.root, "strokes")
        dao = FakeNotebookDao()
        buildUseCase(strokesDir)
    }

    private fun buildUseCase(dir: File) {
        val files = StrokeFileDataSource(dir, UnconfinedTestDispatcher())
        val repo = NotebookRepositoryImpl(dao, FakePageDao(), files)
        useCase = CreateNotebookWithFirstPageUseCase(repo, ids, time)
    }

    private fun strokeFiles(): List<String> = strokesDir.list()?.toList().orEmpty()

    @Test
    fun `happy path creates notebook, first page and stroke file`() = runTest {
        val notebook = useCase("My notes", template = PageTemplate.GRID)

        assertEquals(1, notebook.pageCount)
        val stored = dao.notebooks.single()
        assertEquals(notebook.id, stored.id)
        assertEquals(1, stored.pageCount)
        val page = dao.pages.single()
        assertEquals(0, page.pageIndex)
        assertEquals(notebook.id, page.notebookId)
        assertEquals(PageTemplate.GRID, page.template)
        assertEquals(listOf(page.strokeFileName), strokeFiles())
    }

    @Test
    fun `db failure after file write deletes the file and leaves no notebook`() = runTest {
        dao.failInsert = IllegalStateException("db boom")

        val e = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { useCase("My notes") }
        }

        assertEquals("db boom", e.message)
        assertTrue(dao.notebooks.isEmpty())
        assertTrue(dao.pages.isEmpty())
        assertTrue(strokeFiles().isEmpty())
    }

    @Test
    fun `file failure leaves db untouched`() = runTest {
        // A regular file where the strokes directory should be makes the write fail.
        val blocker = File(tempFolder.root, "blocker").apply { writeText("x") }
        buildUseCase(File(blocker, "strokes"))

        assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { useCase("My notes") }
        }

        assertEquals(0, dao.insertCalls)
        assertTrue(dao.notebooks.isEmpty())
        assertTrue(dao.pages.isEmpty())
    }

    @Test
    fun `blank title is rejected without residue`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { useCase("   ") }
        }
        assertNoResidue()
    }

    @Test
    fun `too long title is rejected without residue`() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { useCase("a".repeat(Notebook.MAX_TITLE_LENGTH + 1)) }
        }
        assertNoResidue()
    }

    @Test
    fun `title at max length is accepted`() = runTest {
        val notebook = useCase("a".repeat(Notebook.MAX_TITLE_LENGTH))
        assertEquals(Notebook.MAX_TITLE_LENGTH, notebook.title.length)
        assertFalse(dao.notebooks.isEmpty())
    }

    private fun assertNoResidue() {
        assertEquals(0, dao.insertCalls)
        assertTrue(dao.notebooks.isEmpty())
        assertTrue(dao.pages.isEmpty())
        assertTrue(strokeFiles().isEmpty())
    }
}

/** In-memory stand-in; [insertWithFirstPage] emulates Room's all-or-nothing transaction. */
private class FakeNotebookDao : NotebookDao() {
    val notebooks = mutableListOf<NotebookEntity>()
    val pages = mutableListOf<PageEntity>()
    var failInsert: Exception? = null
    var insertCalls = 0

    override suspend fun insertWithFirstPage(notebook: NotebookEntity, page: PageEntity) {
        insertCalls++
        failInsert?.let { throw it }
        notebooks += notebook
        pages += page
    }

    override suspend fun nextSortIndex(): Int = notebooks.size

    override fun observeByArchived(archived: Boolean): Flow<List<NotebookEntity>> = TODO()
    override fun observeById(id: String): Flow<NotebookEntity?> = TODO()
    override suspend fun getById(id: String): NotebookEntity? = TODO()
    override suspend fun insert(entity: NotebookEntity) = TODO()
    override suspend fun insertPage(entity: PageEntity) = TODO()
    override suspend fun updateTitle(id: String, title: String, updatedAt: Long): Int = TODO()
    override suspend fun updateArchived(id: String, archived: Boolean, updatedAt: Long): Int = TODO()
    override suspend fun deleteById(id: String): Int = TODO()
}

private class FakePageDao : PageDao() {
    override fun observeByNotebook(notebookId: String): Flow<List<PageEntity>> = TODO()
    override suspend fun getById(id: String): PageEntity? = TODO()
    override suspend fun getPageIds(notebookId: String): List<String> = TODO()
    override suspend fun countByNotebook(notebookId: String): Int = TODO()
    override suspend fun notebookExists(notebookId: String): Boolean = TODO()
    override suspend fun touch(pageId: String, updatedAt: Long): Int = TODO()
    override suspend fun insert(entity: PageEntity) = TODO()
    override suspend fun deleteById(pageId: String) = TODO()
    override suspend fun shiftIndicesUp(notebookId: String, fromIndex: Int) = TODO()
    override suspend fun shiftIndicesDown(notebookId: String, afterIndex: Int) = TODO()
    override suspend fun syncNotebookStats(notebookId: String, now: Long) = TODO()
}
