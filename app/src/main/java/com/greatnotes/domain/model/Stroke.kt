package com.greatnotes.domain.model

/** One continuous pen/highlighter/eraser gesture. Coordinates are in page space (points). */
data class Stroke(
    val id: String,
    val toolType: ToolType,
    val colorArgb: Int,
    val baseWidth: Float,
    val points: List<StrokePoint>,
)

/**
 * A sampled input point.
 *
 * @property pressure normalized stylus pressure in [0, 1].
 * @property tilt stylus tilt in radians, or null when the device does not report it.
 * @property timestampMs event time in milliseconds.
 */
data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val tilt: Float?,
    val timestampMs: Long,
)

enum class ToolType { PEN, HIGHLIGHTER, ERASER_MASK }
