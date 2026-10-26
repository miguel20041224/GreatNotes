package com.greatnotes.domain.repository

import com.greatnotes.domain.model.Stroke

interface StrokeRepository {
    /** Loads all strokes of a page. Returns an empty list when nothing was saved yet. */
    suspend fun loadStrokes(pageId: String): List<Stroke>

    /** Atomically replaces the strokes of a page and touches the page's updatedAt. */
    suspend fun saveStrokes(pageId: String, strokes: List<Stroke>)
}
