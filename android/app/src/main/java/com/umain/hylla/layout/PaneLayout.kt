package com.umain.hylla.layout

import com.umain.hylla.layout.AdaptiveLayout.MIN_PANE_WIDTH_DP
import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.HeightClass
import com.umain.hylla.posture.Posture
import com.umain.hylla.posture.WidthClass
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

        /** Three panes in a flat window: list share and cap, and the history pane's fixed width. */
        const val THREE_PANE_LIST_SHARE = 0.28f
        const val THREE_PANE_LIST_MAX_WIDTH_DP = 420f
        const val HISTORY_WIDTH_DP = 360f

        fun single(posture: WindowPosture) = PaneLayout(listOf(posture.window))

        /**
         * As many panes, up to three, as the window has room for at a legible width.
         *
         * Separating vertical hinges decide the split by themselves: the panes are the segments
         * they leave, so the split is at each hinge, not at a percentage. A tri-fold's two hinges
         * give three panes. Without hinges, a large window gets list, detail and history; a
         * smaller one list and detail; anything narrower one pane.
         */
        fun compute(posture: WindowPosture): PaneLayout {
            val single = single(posture)
            return when (posture.posture) {
                // The cover surface and tabletop have layouts of their own.
                Posture.Cover, Posture.Tabletop -> single
                Posture.Book, Posture.TriFold -> fromSegments(posture.segments) ?: single
                Posture.Flat -> when {
                    posture.heightClass == HeightClass.Compact -> single
                    else -> flatThree(posture) ?: flatSplit(posture) ?: single
                }
            }
        }

        private fun fromSegments(segments: List<DpBounds>): PaneLayout? =
            segments.takeIf { all -> all.size in 2..3 && all.all { it.width >= MIN_PANE_WIDTH_DP } }?.let(::PaneLayout)

        private fun flatThree(posture: WindowPosture): PaneLayout? {
            if (posture.widthClass != WidthClass.Large) return null
            val width = posture.widthDp
            val list = (width * THREE_PANE_LIST_SHARE).coerceIn(MIN_PANE_WIDTH_DP, THREE_PANE_LIST_MAX_WIDTH_DP)
            val historyStart = width - HISTORY_WIDTH_DP
            if (historyStart - list < MIN_PANE_WIDTH_DP) return null
            val window = posture.window
            return PaneLayout(
                listOf(
                    window.copy(right = list),
                    window.copy(left = list, right = historyStart),
                    window.copy(left = historyStart),
                ),
            )
        }

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
