package com.umain.hylla.layout

import com.umain.hylla.posture.HeightClass
import com.umain.hylla.posture.Posture
import com.umain.hylla.posture.WidthClass
import com.umain.hylla.posture.WindowPosture

/** The shape of the top-level navigation. */
enum class ChromeKind { None, BottomBar, Rail, Drawer }

/**
 * Navigation chrome that changes shape with the window: bottom bar, then rail, then drawer.
 *
 * Width alone does not decide it:
 * - A separating hinge or a tabletop crease gets a bottom bar. A rail or drawer on the start edge
 *   would take width from the first segment and move the split off the hinge.
 * - A compact height gets a rail, even when narrow enough for a bar: a phone on its side has no
 *   height to spare for one.
 */
data class ChromeLayout(val kind: ChromeKind) {
    /** Width the chrome takes from the content on the start edge. */
    val startWidthDp: Float = when (kind) {
        ChromeKind.Rail -> RAIL_WIDTH_DP
        ChromeKind.Drawer -> DRAWER_WIDTH_DP
        ChromeKind.None, ChromeKind.BottomBar -> 0f
    }

    companion object {
        const val RAIL_WIDTH_DP = 80f
        const val DRAWER_WIDTH_DP = 240f

        /** A drawer only where its width does not cost a pane: 1600 - 240 is still large. */
        const val DRAWER_MIN_WINDOW_DP = 1600f

        fun compute(posture: WindowPosture): ChromeLayout = ChromeLayout(
            when {
                posture.posture == Posture.Cover -> ChromeKind.None
                posture.posture != Posture.Flat -> ChromeKind.BottomBar
                posture.heightClass == HeightClass.Compact -> ChromeKind.Rail
                posture.widthClass == WidthClass.Compact -> ChromeKind.BottomBar
                posture.widthDp >= DRAWER_MIN_WINDOW_DP -> ChromeKind.Drawer
                else -> ChromeKind.Rail
            },
        )
    }
}

/**
 * The posture of the area the chrome leaves for content. A rail or drawer only appears in flat
 * windows, so there are no hinges to shift; a bottom bar leaves the width untouched.
 */
fun WindowPosture.contentArea(chrome: ChromeLayout): WindowPosture =
    if (chrome.startWidthDp == 0f) this else WindowPosture.compute(widthDp - chrome.startWidthDp, heightDp)
