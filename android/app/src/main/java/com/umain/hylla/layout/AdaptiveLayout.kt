package com.umain.hylla.layout

import com.umain.hylla.posture.WidthClass
import kotlin.math.floor

/**
 * Spacing and column counts that adapt to the space available.
 *
 * Margins and gutters follow the window's width class. Column counts follow the width of the
 * container they fill, never the window, because a pane in a two-pane layout is narrower than the
 * window it sits in.
 */
object AdaptiveLayout {
    /** Below this, a pane is not legible. A layout collapses a pane rather than go narrower. */
    const val MIN_PANE_WIDTH_DP = 360f

    /** Minimum width of one fleet tile at 100% text size. */
    const val FLEET_TILE_MIN_WIDTH_DP = 280f

    /** Minimum width of one detail field at 100% text size. */
    const val DETAIL_FIELD_MIN_WIDTH_DP = 220f

    /** Column minimums grow with text size up to this factor, then stop. */
    const val MAX_TEXT_SCALE = 2f

    fun margin(widthClass: WidthClass): Float = when (widthClass) {
        WidthClass.Compact -> 16f
        WidthClass.Medium, WidthClass.Expanded, WidthClass.Large -> 24f
    }

    fun gutter(widthClass: WidthClass): Float = when (widthClass) {
        WidthClass.Compact -> 12f
        WidthClass.Medium, WidthClass.Expanded, WidthClass.Large -> 16f
    }

    /**
     * How many columns of at least [minColumnWidth] fit in [availableWidth], with [gutter] between
     * them. Always at least one. A wider container gains columns; each column never grows past
     * roughly twice its minimum, so nothing stretches.
     *
     * [textScale] is the user's text size relative to default. Larger text needs wider columns to
     * stay legible, so the minimum scales with it, capped at [MAX_TEXT_SCALE].
     */
    fun columns(availableWidth: Float, minColumnWidth: Float, gutter: Float, textScale: Float = 1f): Int {
        val minimum = minColumnWidth * textScale.coerceIn(1f, MAX_TEXT_SCALE)
        return floor((availableWidth + gutter) / (minimum + gutter)).toInt().coerceAtLeast(1)
    }
}
