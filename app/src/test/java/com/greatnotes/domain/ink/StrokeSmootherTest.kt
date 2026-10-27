package com.greatnotes.domain.ink

import com.greatnotes.domain.model.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StrokeSmootherTest {

    private fun point(x: Float, y: Float = 0f, pressure: Float = 0.5f, tilt: Float? = null, t: Long = 0L) =
        StrokePoint(x = x, y = y, pressure = pressure, tilt = tilt, timestampMs = t)

    @Test
    fun emptyInputIsReturnedUnchanged() {
        assertEquals(emptyList<StrokePoint>(), StrokeSmoother().smooth(emptyList()))
    }

    @Test
    fun singlePointIsReturnedUnchanged() {
        val p = point(3f, 4f, 0.7f, 0.1f, 9L)
        assertEquals(listOf(p), StrokeSmoother().smooth(listOf(p)))
    }

    @Test
    fun firstAndLastPointsArePreservedExactly() {
        val input = listOf(point(0f, 0f), point(10f, 5f), point(0f, 20f), point(30f, 1f), point(7f, 7f))
        val out = StrokeSmoother(windowSize = 5).smooth(input)
        assertEquals(input.first(), out.first())
        assertEquals(input.last(), out.last())
        assertEquals(input.size, out.size)
    }

    @Test
    fun windowOfOneIsIdentity() {
        val input = listOf(point(0f), point(5f, 2f), point(1f, 9f))
        assertEquals(input, StrokeSmoother(windowSize = 1).smooth(input))
    }

    @Test
    fun spikeIsAttenuatedWithWeightedAverage() {
        val input = listOf(point(0f, 0f), point(1f, 0f), point(2f, 10f), point(3f, 0f), point(4f, 0f))
        val out = StrokeSmoother(windowSize = 3).smooth(input)
        // weights 1,2,1 over (0, 10, 0) -> 2*10/4 = 5
        assertEquals(5f, out[2].y, 1e-5f)
    }

    @Test
    fun straightLineIsPreserved() {
        val input = (0..9).map { point(it * 2f, it * 3f) }
        val out = StrokeSmoother(windowSize = 5).smooth(input)
        out.zip(input).forEach { (o, i) ->
            assertEquals(i.x, o.x, 1e-4f)
            assertEquals(i.y, o.y, 1e-4f)
        }
    }

    @Test
    fun pressureIsSmoothed() {
        val input = listOf(
            point(0f, pressure = 0f),
            point(1f, pressure = 0f),
            point(2f, pressure = 1f),
            point(3f, pressure = 0f),
            point(4f, pressure = 0f),
        )
        val out = StrokeSmoother(windowSize = 3).smooth(input)
        assertEquals(0.5f, out[2].pressure, 1e-5f)
    }

    @Test
    fun tiltAndTimestampComeFromSourcePoint() {
        val input = listOf(
            point(0f, tilt = 0.1f, t = 10L),
            point(1f, tilt = null, t = 20L),
            point(2f, tilt = 0.3f, t = 30L),
        )
        val out = StrokeSmoother(windowSize = 3).smooth(input)
        assertEquals(listOf(0.1f, null, 0.3f), out.map { it.tilt })
        assertEquals(listOf(10L, 20L, 30L), out.map { it.timestampMs })
        assertNull(out[1].tilt)
    }

    @Test
    fun windowLargerThanInputStillKeepsEndpointsAndSize() {
        val input = listOf(point(0f), point(4f, 8f), point(8f))
        val out = StrokeSmoother(windowSize = 15).smooth(input)
        assertEquals(3, out.size)
        assertEquals(input.first(), out.first())
        assertEquals(input.last(), out.last())
        assertTrue(out[1].y < 8f)
    }

    @Test
    fun invalidWindowSizesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { StrokeSmoother(windowSize = 0) }
        assertThrows(IllegalArgumentException::class.java) { StrokeSmoother(windowSize = -3) }
        assertThrows(IllegalArgumentException::class.java) { StrokeSmoother(windowSize = 4) }
    }
}
