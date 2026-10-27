package com.greatnotes.domain.ink

import com.greatnotes.domain.model.Stroke
import com.greatnotes.domain.model.ToolType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StrokeHistoryTest {

    private fun stroke(id: String) = Stroke(
        id = id,
        toolType = ToolType.PEN,
        colorArgb = 0xFF000000.toInt(),
        baseWidth = 2f,
        points = emptyList(),
    )

    private fun ids(history: StrokeHistory) = history.strokes.map { it.id }

    @Test
    fun newHistoryIsEmptyAndCannotUndoOrRedo() {
        val h = StrokeHistory()
        assertTrue(h.strokes.isEmpty())
        assertFalse(h.canUndo)
        assertFalse(h.canRedo)
    }

    @Test
    fun initialStrokesAreKeptWithoutHistory() {
        val h = StrokeHistory(strokes = listOf(stroke("a")))
        assertEquals(listOf("a"), ids(h))
        assertFalse(h.canUndo)
    }

    @Test
    fun addStrokeAppendsAndEnablesUndo() {
        val h = StrokeHistory().execute(CanvasCommand.AddStroke(stroke("a")))
        assertEquals(listOf("a"), ids(h))
        assertTrue(h.canUndo)
        assertFalse(h.canRedo)
    }

    @Test
    fun undoThenRedoRoundTripsAddStroke() {
        val added = StrokeHistory().execute(CanvasCommand.AddStroke(stroke("a")))
        val undone = added.undo()
        assertTrue(undone.strokes.isEmpty())
        assertTrue(undone.canRedo)
        assertFalse(undone.canUndo)
        assertEquals(added.strokes, undone.redo().strokes)
    }

    @Test
    fun undoAndRedoOnEmptyStacksAreNoOps() {
        val h = StrokeHistory(strokes = listOf(stroke("a")))
        assertSame(h, h.undo())
        assertSame(h, h.redo())
    }

    @Test
    fun executingNewCommandClearsRedoStack() {
        val h = StrokeHistory()
            .execute(CanvasCommand.AddStroke(stroke("a")))
            .undo()
        assertTrue(h.canRedo)
        val next = h.execute(CanvasCommand.AddStroke(stroke("b")))
        assertFalse(next.canRedo)
        assertEquals(listOf("b"), ids(next))
    }

    @Test
    fun eraseRemovesStrokesById() {
        val h = StrokeHistory(strokes = listOf(stroke("a"), stroke("b"), stroke("c")))
            .execute(CanvasCommand.EraseStrokes(listOf(stroke("b"))))
        assertEquals(listOf("a", "c"), ids(h))
    }

    @Test
    fun undoEraseRestoresNonContiguousStrokesAtOriginalIndices() {
        val start = StrokeHistory(strokes = listOf("a", "b", "c", "d", "e", "f").map(::stroke))
        // Passed out of order on purpose: restoration must not depend on command order.
        val erased = start.execute(CanvasCommand.EraseStrokes(listOf(stroke("e"), stroke("b"), stroke("d"))))
        assertEquals(listOf("a", "c", "f"), ids(erased))
        val restored = erased.undo()
        assertEquals(listOf("a", "b", "c", "d", "e", "f"), ids(restored))
        assertEquals(listOf("a", "c", "f"), ids(restored.redo()))
    }

    @Test
    fun undoEraseKeepsStrokesAddedBeforeIt() {
        val h = StrokeHistory(strokes = listOf(stroke("a"), stroke("b")))
            .execute(CanvasCommand.AddStroke(stroke("c")))
            .execute(CanvasCommand.EraseStrokes(listOf(stroke("a"))))
            .undo()
        assertEquals(listOf("a", "b", "c"), ids(h))
        assertEquals(listOf("a", "b"), ids(h.undo()))
    }

    @Test
    fun eraseOfUnknownStrokesIsNoOpWithoutHistoryEntry() {
        val h = StrokeHistory(strokes = listOf(stroke("a")))
        val after = h.execute(CanvasCommand.EraseStrokes(listOf(stroke("zzz"))))
        assertSame(h, after)
        assertFalse(after.canUndo)
    }

    @Test
    fun maxDepthDropsOldestEntries() {
        var h = StrokeHistory(maxDepth = 2)
        listOf("a", "b", "c").forEach { h = h.execute(CanvasCommand.AddStroke(stroke(it))) }
        assertEquals(listOf("a", "b", "c"), ids(h))
        h = h.undo().undo()
        assertEquals(listOf("a"), ids(h))
        assertFalse(h.canUndo)
        assertSame(h, h.undo())
    }

    @Test
    fun invalidMaxDepthIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { StrokeHistory(maxDepth = 0) }
    }
}
