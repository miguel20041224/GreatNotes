package com.greatnotes.domain.usecase

import com.greatnotes.domain.model.Page
import com.greatnotes.domain.model.PageTemplate
import com.greatnotes.domain.repository.PageRepository
import com.greatnotes.domain.util.IdGenerator
import com.greatnotes.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow

class ObservePagesUseCase(private val repository: PageRepository) {
    operator fun invoke(notebookId: String): Flow<List<Page>> = repository.observePages(notebookId)
}

class GetPageUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(pageId: String): Page? = repository.getPage(pageId)
}

class AddPageUseCase(
    private val repository: PageRepository,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) {
    /**
     * @throws IllegalArgumentException if the size is not a positive finite number.
     * @throws NoSuchElementException if the notebook does not exist.
     */
    suspend operator fun invoke(
        notebookId: String,
        template: PageTemplate = PageTemplate.BLANK,
        widthPt: Float = Page.DEFAULT_WIDTH_PT,
        heightPt: Float = Page.DEFAULT_HEIGHT_PT,
        insertAt: Int = Page.APPEND_INDEX,
    ): Page {
        require(widthPt.isFinite() && widthPt > 0f) { "widthPt must be positive and finite" }
        require(heightPt.isFinite() && heightPt > 0f) { "heightPt must be positive and finite" }
        val id = idGenerator.newId()
        val now = timeProvider.nowMs()
        return repository.addPage(
            Page(
                id = id,
                notebookId = notebookId,
                pageIndex = insertAt,
                template = template,
                widthPt = widthPt,
                heightPt = heightPt,
                strokeFileName = Page.strokeFileNameFor(id),
                pdfSourceUri = null,
                pdfPageIndex = null,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }
}

class DeletePageUseCase(
    private val repository: PageRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(pageId: String): Boolean =
        repository.deletePage(pageId, timeProvider.nowMs())
}
