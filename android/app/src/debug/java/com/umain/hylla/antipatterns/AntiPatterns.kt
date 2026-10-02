package com.umain.hylla.antipatterns

import com.umain.hylla.layout.PaneLayout
import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.Fold
import com.umain.hylla.posture.WindowPosture

/**
 * The four mistakes chapter 19 is about, written on purpose, in debug builds only. Each was seen
 * producing a real rendering bug on real hardware; the tests next to them show each one failing
 * where the correct rule does not.
 */
object AntiPatterns {

    /** Wrong: landscape means two panes. Orientation is not size. */
    fun panesByOrientation(widthDp: Float, heightDp: Float): Int = if (widthDp > heightDp) 2 else 1

    /**
     * Wrong: lay out for the display, read from `Resources.displayMetrics` or
     * `LocalConfiguration.screenWidthDp` captured at launch, instead of for the window. In split
     * screen or a free-form window the display is wider than the window.
     */
    fun panesByDisplay(displayWidthDp: Float, windowHeightDp: Float): Int =
        PaneLayout.compute(WindowPosture.compute(displayWidthDp, windowHeightDp)).paneCount

    /** Wrong: `displayFeatures.firstOrNull()`. A tri-fold has two hinges. */
    fun segmentsFromFirstHinge(window: DpBounds, folds: List<Fold>): List<DpBounds> =
        folds.firstOrNull { it.isSeparating }?.let { window.splitAt(listOf(it)) } ?: listOf(window)
}
