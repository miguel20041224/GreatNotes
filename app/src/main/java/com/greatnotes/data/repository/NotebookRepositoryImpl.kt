package com.greatnotes.data.repository

import com.greatnotes.data.local.dao.NotebookDao
import com.greatnotes.data.local.dao.PageDao
import com.greatnotes.data.local.file.StrokeFileDataSource
import com.greatnotes.data.mapper.toDomain
import com.greatnotes.data.mapper.toEntity
import com.greatnotes.domain.model.Page
import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.repository.NotebookRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotebookRepositoryImpl @Inject constructor(
    private val notebookDao: NotebookDao,
    private val pageDao: PageDao,
    private val strokeFiles: StrokeFileDataSource,
) : NotebookRepository {

    override fun observeNotebooks(archived: Boolean): Flow<List<Notebook>> =
        notebookDao.observeByArchived(archived).map { list -> list.map { it.toDomain() } }

    override fun observeNotebook(id: String): Flow<Notebook?> =
        notebookDao.observeById(id).map { it?.toDomain() }

    override suspend fun getNotebook(id: String): Notebook? = notebookDao.getById(id)?.toDomain()

    override suspend fun nextSortIndex(): Int = notebookDao.nextSortIndex()

    override suspend fun createNotebook(notebook: Notebook) = notebookDao.insert(notebook.toEntity())

    /**
     * Order is fixed: (1) write the empty stroke file (fail-fast on full disk or bad permissions,
     * DB untouched), (2) run the Room transaction, (3) if it fails, delete the file best-effort
     * and rethrow the original exception. The worst case is an inert orphan file, never an
     * empty notebook.
     */
    override suspend fun createNotebookWithFirstPage(notebook: Notebook, firstPage: Page) {
        require(firstPage.notebookId == notebook.id) { "firstPage must belong to the notebook" }
        require(firstPage.pageIndex == 0) { "firstPage must have pageIndex 0" }
        require(notebook.pageCount == 1) { "notebook must have pageCount 1" }

        strokeFiles.write(firstPage.id, emptyList())
        try {
            notebookDao.insertWithFirstPage(notebook.toEntity(), firstPage.toEntity())
        } catch (e: Exception) {
            try {
                // NonCancellable: cleanup must run even if the failure was a cancellation.
                withContext(NonCancellable) { strokeFiles.deleteBestEffort(firstPage.id) }
            } catch (cleanup: Exception) {
                e.addSuppressed(cleanup)
            }
            throw e
        }
    }

    override suspend fun renameNotebook(id: String, title: String, updatedAt: Long): Boolean =
        notebookDao.updateTitle(id, title, updatedAt) > 0

    override suspend fun setArchived(id: String, archived: Boolean, updatedAt: Long): Boolean =
        notebookDao.updateArchived(id, archived, updatedAt) > 0

    /**
     * The DB rows are deleted first (source of truth); stroke files are removed afterwards.
     * A crash in between only leaves orphan files, never dangling references.
     */
    override suspend fun deleteNotebook(id: String) {
        val pageIds = pageDao.getPageIds(id)
        notebookDao.deleteById(id)
        pageIds.forEach { strokeFiles.deleteBestEffort(it) }
    }
}
