package com.greatnotes.domain.ink

import com.greatnotes.domain.model.Stroke

/** An undoable mutation of a page's ordered stroke list. Strokes are matched by [Stroke.id]. */
sealed interface CanvasCommand {

    /** Appends [stroke] on top of the z-order. */
    data class AddStroke(val stroke: Stroke) : CanvasCommand

    /** Removes [strokes]; undo restores each one at its original index. */
    data class EraseStrokes(val strokes: List<Stroke>) : CanvasCommand
}
