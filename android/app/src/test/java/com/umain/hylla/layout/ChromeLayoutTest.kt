package com.umain.hylla.layout

import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.Fold
import com.umain.hylla.posture.FoldOcclusion
import com.umain.hylla.posture.FoldOrientation
import com.umain.hylla.posture.FoldState
import com.umain.hylla.posture.WindowPosture
import org.junit.Assert.assertEquals
import org.junit.Test

class ChromeLayoutTest {

    private fun hinge(at: Float, length: Float, vertical: Boolean) = Fold(
        bounds = if (vertical) DpBounds(at, 0f, at, length) else DpBounds(0f, at, length, at),
        orientation = if (vertical) FoldOrientation.Vertical else FoldOrientation.Horizontal,
        state = FoldState.HalfOpened,
        isSeparating = true,
        occlusion = FoldOcclusion.None,
    )

    private fun chrome(width: Float, height: Float, vararg folds: Fold) =
        ChromeLayout.compute(WindowPosture.compute(width, height, folds.toList())).kind

    @Test
    fun `phone portrait gets a bottom bar`() {
        assertEquals(ChromeKind.BottomBar, chrome(411f, 891f))
    }

    @Test
    fun `phone landscape gets a rail, not a bar, because the height is compact`() {
        assertEquals(ChromeKind.Rail, chrome(891f, 411f))
    }

    @Test
    fun `medium and expanded flat windows get a rail`() {
        assertEquals(ChromeKind.Rail, chrome(700f, 900f))
        assertEquals(ChromeKind.Rail, chrome(852f, 883f))
        assertEquals(ChromeKind.Rail, chrome(1376f, 1032f))
    }

    @Test
    fun `a drawer only from 1600, where its width does not cost a pane`() {
        assertEquals(ChromeKind.Rail, chrome(1599f, 1000f))
        assertEquals(ChromeKind.Drawer, chrome(1600f, 1000f))
    }

    @Test
    fun `book posture gets a bottom bar so the split stays on the hinge`() {
        assertEquals(ChromeKind.BottomBar, chrome(852f, 883f, hinge(426f, 883f, vertical = true)))
    }

    @Test
    fun `tabletop gets a bottom bar`() {
        assertEquals(ChromeKind.BottomBar, chrome(883f, 852f, hinge(426f, 883f, vertical = false)))
    }

    @Test
    fun `the cover has no chrome`() {
        assertEquals(ChromeKind.None, chrome(332f, 348f))
    }

    @Test
    fun `a rail narrows the content area that panes are laid out in`() {
        val window = WindowPosture.compute(852f, 883f)
        val content = window.contentArea(ChromeLayout.compute(window))

        assertEquals(772f, content.widthDp)
        assertEquals(listOf(360f, 412f), PaneLayout.compute(content).panes.map { it.width })
    }

    @Test
    fun `a drawer still leaves room for three panes`() {
        val window = WindowPosture.compute(1600f, 1000f)
        val content = window.contentArea(ChromeLayout.compute(window))

        assertEquals(3, PaneLayout.compute(content).paneCount)
    }
}
