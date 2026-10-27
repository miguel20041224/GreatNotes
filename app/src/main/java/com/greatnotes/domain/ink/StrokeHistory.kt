package com.greatnotes.domain.ink

import com.greatnotes.domain.model.Stroke

/**
 * Immutable undo/redo state for a page canvas: the current [strokes] plus undo and redo stacks.
 * Every operation returns a new instance (or `this` when it is a no-op).
 *
 * @property maxDepth maximum number of undo entries kept (oldest dropped), or null for unlimited.
 */
class StrokeHistory private constructor(
    val strokes: List<Stroke>,
    private val undoStack: List<Entry>,
    private val redoStack: List<Entry>,
    val maxDepth: Int?,
) {

    constructor(strokes: List<Stroke> = emptyList(), maxDepth: Int? = null) :
        this(strokes, emptyList(), emptyList(), maxDepth)

    init {
        require(maxDepth == null || maxDepth >= 1) { "maxDepth must be >= 1, was $maxDepth" }
    }

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** Applies [command] and clears the redo stack. Commands with no effect are ignored. */
    fun execute(command: CanvasCommand): StrokeHistory {
        val (next, entry) = apply(strokes, command) ?: return this
        return StrokeHistory(next, trim(undoStack + entry), emptyList(), maxDepth)
    }

    fun undo(): StrokeHistory {
        val entry = undoStack.lastOrNull() ?: return this
        return StrokeHistory(revert(strokes, entry), undoStack.dropLast(1), redoStack + entry, maxDepth)
    }

    fun redo(): StrokeHistory {
        val entry = redoStack.lastOrNull() ?: return this
        // State equals the state before the command, so re-applying reproduces the same entry.
        val (next, applied) = apply(strokes, entry.command) ?: return this
        return StrokeHistory(next, trim(undoStack + applied), redoStack.dropLast(1), maxDepth)
    }

    private fun trim(stack: List<Entry>): List<Entry> =
        if (maxDepth != null && stack.size > maxDepth) stack.drop(stack.size - maxDepth) else stack

    /** An applied command plus what is needed to revert it: erased strokes with their indices. */
    private class Entry(val command: CanvasCommand, val erased: List<IndexedValue<Stroke>>)

    private fun apply(current: List<Stroke>, command: CanvasCommand): Pair<List<Stroke>, Entry>? =
        when (command) {
            is CanvasCommand.AddStroke -> (current + command.stroke) to Entry(command, emptyList())
            is CanvasCommand.EraseStrokes -> {
                val ids = command.strokes.mapTo(HashSet()) { it.id }
                val erased = current.withIndex().filter { it.value.id in ids }
                if (erased.isEmpty()) {
                    null
                } else {
                    current.filter { it.id !in ids } to Entry(command, erased)
                }
            }
        }

    private fun revert(current: List<Stroke>, entry: Entry): List<Stroke> =
        when (val command = entry.command) {
            is CanvasCommand.AddStroke -> current.filter { it.id != command.stroke.id }
            is CanvasCommand.EraseStrokes -> {
                // Ascending original indices: each insertion lands at its final position.
                val result = current.toMutableList()
                entry.erased.forEach { (index, stroke) -> result.add(index, stroke) }
                result
            }
        }
}
