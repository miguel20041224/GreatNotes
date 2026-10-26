package com.greatnotes.domain.repository

import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.Page
import kotlinx.coroutines.flow.Flow

interface NotebookRepository {
    /** Emits notebooks ordered by sortIndex, then most recently updated. Re-emits on changes. */
    fun observeNotebooks(archived: Boolean = false): Flow<List<Notebook>>

    /** Emits null when the notebook does not exist (or is deleted). */
    fun observeNotebook(id: String): Flow<Notebook?>

    suspend fun getNotebook(id: String): Notebook?

    /** Next free sortIndex (max + 1, or 0 when there are no notebooks). */
    suspend fun nextSortIndex(): Int

    /** Inserts a new notebook. Fails if the id already exists. */
    suspend fun createNotebook(notebook: Notebook)

    /**
     * Persists [notebook] together with its [firstPage] so that a notebook never exists without
     * pages. Requires `firstPage.notebookId == notebook.id`, `firstPage.pageIndex == 0` and
     * `notebook.pageCount == 1`.
     *
     * Guarantee: on any failure (including cancellation) the notebook and page rows do not exist
     * and the exception is propagated. The stroke file of the page may linger only if the
     * best-effort cleanup itself fails (an inert, empty orphan file).
     *
     * @throws IllegalArgumentException if the arguments violate the requirements above.
     */
    suspend fun createNotebookWithFirstPage(notebook: Notebook, firstPage: Page)

    /** @return true if a notebook was updated. */
    suspend fun renameNotebook(id: String, title: String, updatedAt: Long): Boolean

    /** @return true if a notebook was updated. */
    suspend fun setArchived(id: String, archived: Boolean, updatedAt: Long): Boolean

    /** Deletes the notebook, its pages and (best effort) their stroke files. */
    suspend fun deleteNotebook(id: String)
}
