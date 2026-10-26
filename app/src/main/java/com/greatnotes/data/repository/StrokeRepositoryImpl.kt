package com.greatnotes.data.repository

import com.greatnotes.data.local.dao.PageDao
import com.greatnotes.data.local.file.StrokeFileDataSource
import com.greatnotes.domain.model.Stroke
import com.greatnotes.domain.repository.StrokeRepository
import com.greatnotes.domain.util.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StrokeRepositoryImpl @Inject constructor(
    private val strokeFiles: StrokeFileDataSource,
    private val pageDao: PageDao,
    private val timeProvider: TimeProvider,
) : StrokeRepository {

    override suspend fun loadStrokes(pageId: String): List<Stroke> = strokeFiles.read(pageId)

    override suspend fun saveStrokes(pageId: String, strokes: List<Stroke>) {
        strokeFiles.write(pageId, strokes)
        pageDao.touch(pageId, timeProvider.nowMs())
    }
}
