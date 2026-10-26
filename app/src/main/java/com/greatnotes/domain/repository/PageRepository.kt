package com.greatnotes.domain.repository

import com.greatnotes.domain.model.Page
import kotlinx.coroutines.flow.Flow

interface PageRepository {
    /** Emits the pages of a notebook ordered by pageIndex. */
    fun observePages(notebookId: String): Flow<List<Page>>

    suspend fun getPage(pageId: String): Page?

    /**
     * Inserts [page] at its pageIndex, shifting later pages. The index is clamped to
     * [0, pageCount]; use [Page.APPEND_INDEX] to append. The notebook's pageCount and
     * updatedAt are updated atomically.
     *
     * @return the stored page, with the effective pageIndex.
     * @throws NoSuchElementException if the notebook does not exist.
     */
    suspend fun addPage(page: Page): Page

    /**
     * Deletes the page, closes the index gap and (best effort) removes its stroke file.
     *
     * @return true if the page existed.
     */
    suspend fun deletePage(pageId: String, updatedAt: Long): Boolean
}
