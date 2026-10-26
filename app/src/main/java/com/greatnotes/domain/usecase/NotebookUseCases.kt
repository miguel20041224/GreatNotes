package com.greatnotes.domain.usecase

import com.greatnotes.domain.model.Notebook
import com.greatnotes.domain.model.NotebookCover
import com.greatnotes.domain.model.Page
import com.greatnotes.domain.model.PageTemplate
import com.greatnotes.domain.repository.NotebookRepository
import com.greatnotes.domain.util.IdGenerator
import com.greatnotes.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow

class ObserveNotebooksUseCase(private val repository: NotebookRepository) {
    operator fun invoke(archived: Boolean = false): Flow<List<Notebook>> =
        repository.observeNotebooks(archived)
}

class ObserveNotebookUseCase(private val repository: NotebookRepository) {
    operator fun invoke(id: String): Flow<Notebook?> = repository.observeNotebook(id)
}

class CreateNotebookUseCase(
    private val repository: NotebookRepository,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) {
    /** @throws IllegalArgumentException if the title is blank or too long. */
    suspend operator fun invoke(
        title: String,
        cover: NotebookCover = NotebookCover.DEFAULT,
    ): Notebook {
        val cleanTitle = validateTitle(title)
        val now = timeProvider.nowMs()
        val notebook = Notebook(
            id = idGenerator.newId(),
            title = cleanTitle,
            cover = cover,
            createdAt = now,
            updatedAt = now,
            pageCount = 0,
            isArchived = false,
            sortIndex = repository.nextSortIndex(),
        )
        repository.createNotebook(notebook)
        return notebook
    }
}

/**
 * Creates a notebook together with its first page, atomically from the caller's perspective:
 * on failure no notebook is left behind (see [NotebookRepository.createNotebookWithFirstPage]).
 */
class CreateNotebookWithFirstPageUseCase(
    private val repository: NotebookRepository,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) {
    /**
     * @return the created notebook (pageCount = 1).
     * @throws IllegalArgumentException if the title is blank or too long; nothing is persisted.
     */
    suspend operator fun invoke(
        title: String,
        cover: NotebookCover = NotebookCover.DEFAULT,
        template: PageTemplate = PageTemplate.BLANK,
    ): Notebook {
        val cleanTitle = validateTitle(title)
        val now = timeProvider.nowMs()
        val notebook = Notebook(
            id = idGenerator.newId(),
            title = cleanTitle,
            cover = cover,
            createdAt = now,
            updatedAt = now,
            pageCount = 1,
            isArchived = false,
            sortIndex = repository.nextSortIndex(),
        )
        val pageId = idGenerator.newId()
        val page = Page(
            id = pageId,
            notebookId = notebook.id,
            pageIndex = 0,
            template = template,
            widthPt = Page.DEFAULT_WIDTH_PT,
            heightPt = Page.DEFAULT_HEIGHT_PT,
            strokeFileName = Page.strokeFileNameFor(pageId),
            pdfSourceUri = null,
            pdfPageIndex = null,
            createdAt = now,
            updatedAt = now,
        )
        repository.createNotebookWithFirstPage(notebook, page)
        return notebook
    }
}

class RenameNotebookUseCase(
    private val repository: NotebookRepository,
    private val timeProvider: TimeProvider,
) {
    /** @return true if the notebook existed. @throws IllegalArgumentException on invalid title. */
    suspend operator fun invoke(id: String, title: String): Boolean =
        repository.renameNotebook(id, validateTitle(title), timeProvider.nowMs())
}

class ArchiveNotebookUseCase(
    private val repository: NotebookRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(id: String, archived: Boolean = true): Boolean =
        repository.setArchived(id, archived, timeProvider.nowMs())
}

class DeleteNotebookUseCase(private val repository: NotebookRepository) {
    suspend operator fun invoke(id: String) = repository.deleteNotebook(id)
}

private fun validateTitle(title: String): String {
    val clean = title.trim()
    require(clean.isNotEmpty()) { "Notebook title must not be blank" }
    require(clean.length <= Notebook.MAX_TITLE_LENGTH) {
        "Notebook title must be at most ${Notebook.MAX_TITLE_LENGTH} characters"
    }
    return clean
}
