package com.umain.hylla.layout

import com.umain.hylla.layout.AdaptiveLayout.MIN_PANE_WIDTH_DP
import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.HeightClass
import com.umain.hylla.posture.Posture
import com.umain.hylla.posture.WindowPosture

/**
 * How many panes the window shows side by side, and where each one sits, in window
 * coordinates. One entry means one pane at a time, reached by navigation.
 */
data class PaneLayout(val panes: List<DpBounds>) {
    val paneCount: Int get() = panes.size

    companion object {
        /** The list pane in a flat split: this share of the width, clamped to the range below. */
        const val LIST_SHARE = 0.4f
        const val LIST_MAX_WIDTH_DP = 480f

        fun single(posture: WindowPosture) = PaneLayout(listOf(posture.window))

        /**
         * Two panes when the window has room for two legible ones, else one.
         *
         * A separating vertical hinge decides the split by itself: the panes are the segments the
         * hinge leaves, so the split is at the hinge, not at half. Without one, the list takes
         * [LIST_SHARE] of the width and the detail the rest.
         */
        fun compute(posture: WindowPosture): PaneLayout {
            val single = single(posture)
            return when (posture.posture) {
                // The cover surface and tabletop have layouts of their own.
                Posture.Cover, Posture.Tabletop -> single
                Posture.Book -> fromSegments(posture.segments) ?: single
                Posture.TriFold -> single
                Posture.Flat -> if (posture.heightClass == HeightClass.Compact) single else flatSplit(posture) ?: single
            }
        }

        private fun fromSegments(segments: List<DpBounds>): PaneLayout? =
            segments.takeIf { all -> all.size == 2 && all.all { it.width >= MIN_PANE_WIDTH_DP } }?.let(::PaneLayout)

        private fun flatSplit(posture: WindowPosture): PaneLayout? {
            val width = posture.widthDp
            val list = (width * LIST_SHARE).coerceIn(MIN_PANE_WIDTH_DP, LIST_MAX_WIDTH_DP)
            if (width - list < MIN_PANE_WIDTH_DP) return null
            val window = posture.window
            return PaneLayout(listOf(window.copy(right = list), window.copy(left = list)))
        }
    }
}

val WindowPosture.window: DpBounds get() = DpBounds(0f, 0f, widthDp, heightDp)
