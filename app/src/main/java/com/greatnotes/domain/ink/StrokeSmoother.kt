package com.greatnotes.domain.ink

import com.greatnotes.domain.model.StrokePoint

/**
 * Smooths raw stylus samples with a triangular-weighted moving average over x, y and pressure.
 *
 * The window is symmetric and shrinks near the stroke ends (radius = min(radius, distance to
 * the nearest end)), so the first and last points are returned exactly and the stroke does not
 * shrink. Tilt and timestamp are always copied from the source point.
 *
 * @property windowSize number of samples in the averaging window; must be odd and >= 1.
 * A value of 1 is the identity.
 */
class StrokeSmoother(val windowSize: Int = DEFAULT_WINDOW_SIZE) {

    init {
        require(windowSize >= 1 && windowSize % 2 == 1) {
            "windowSize must be a positive odd number, was $windowSize"
        }
    }

    private val radius = windowSize / 2

    fun smooth(points: List<StrokePoint>): List<StrokePoint> {
        if (points.size < 2 || radius == 0) return points
        val last = points.lastIndex
        return List(points.size) { i ->
            val r = minOf(radius, i, last - i)
            if (r == 0) {
                points[i]
            } else {
                var wSum = 0f
                var x = 0f
                var y = 0f
                var p = 0f
                for (k in -r..r) {
                    val w = (r + 1 - kotlin.math.abs(k)).toFloat()
                    val src = points[i + k]
                    wSum += w
                    x += w * src.x
                    y += w * src.y
                    p += w * src.pressure
                }
                points[i].copy(x = x / wSum, y = y / wSum, pressure = p / wSum)
            }
        }
    }

    companion object {
        const val DEFAULT_WINDOW_SIZE = 5
    }
}
