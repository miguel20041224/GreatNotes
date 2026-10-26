package com.greatnotes.domain.usecase

import com.greatnotes.domain.model.Stroke
import com.greatnotes.domain.repository.StrokeRepository

class LoadStrokesUseCase(private val repository: StrokeRepository) {
    suspend operator fun invoke(pageId: String): List<Stroke> = repository.loadStrokes(pageId)
}

class SaveStrokesUseCase(private val repository: StrokeRepository) {
    suspend operator fun invoke(pageId: String, strokes: List<Stroke>) =
        repository.saveStrokes(pageId, strokes)
}
