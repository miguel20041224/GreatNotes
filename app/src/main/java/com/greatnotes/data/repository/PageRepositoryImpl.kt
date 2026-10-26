package com.greatnotes.data.repository

import com.greatnotes.data.local.dao.PageDao
import com.greatnotes.data.local.file.StrokeFileDataSource
import com.greatnotes.data.mapper.toDomain
import com.greatnotes.data.mapper.toEntity
import com.greatnotes.domain.model.Page
import com.greatnotes.domain.repository.PageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageRepositoryImpl @Inject constructor(
    private val pageDao: PageDao,
    private val strokeFiles: StrokeFileDataSource,
) : PageRepository {

    override fun observePages(notebookId: String): Flow<List<Page>> =
        pageDao.observeByNotebook(notebookId).map { list -> list.map { it.toDomain() } }

    override suspend fun getPage(pageId: String): Page? = pageDao.getById(pageId)?.toDomain()

    override suspend fun addPage(page: Page): Page = pageDao.insertAt(page.toEntity()).toDomain()

    override suspend fun deletePage(pageId: String, updatedAt: Long): Boolean {
        val deleted = pageDao.deleteAndCompact(pageId, updatedAt)
        if (deleted) strokeFiles.deleteBestEffort(pageId)
        return deleted
    }
}
