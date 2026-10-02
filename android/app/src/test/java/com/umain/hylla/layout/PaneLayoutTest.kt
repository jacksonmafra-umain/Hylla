package com.umain.hylla.layout

import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.Fold
import com.umain.hylla.posture.FoldOcclusion
import com.umain.hylla.posture.FoldOrientation
import com.umain.hylla.posture.FoldState
import com.umain.hylla.posture.WindowPosture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaneLayoutTest {

    private fun hinge(x: Float, height: Float, width: Float = 0f, vertical: Boolean = true) = Fold(
        bounds = if (vertical) DpBounds(x, 0f, x + width, height) else DpBounds(0f, x, height, x + width),
        orientation = if (vertical) FoldOrientation.Vertical else FoldOrientation.Horizontal,
        state = FoldState.HalfOpened,
        isSeparating = true,
        occlusion = if (width > 0f) FoldOcclusion.Full else FoldOcclusion.None,
    )

    private fun panes(width: Float, height: Float, vararg folds: Fold) =
        PaneLayout.compute(WindowPosture.compute(width, height, folds.toList()))

    @Test
    fun `phone portrait is one pane`() {
        assertEquals(1, panes(411f, 891f).paneCount)
    }

    @Test
    fun `phone landscape stays one pane although it is wide, because the height is compact`() {
        assertEquals(1, panes(891f, 411f).paneCount)
    }

    @Test
    fun `a medium window too narrow for two legible panes collapses to one`() {
        assertEquals(1, panes(700f, 900f).paneCount)
    }

    @Test
    fun `flat foldable splits 40 to 60 with the list at its minimum`() {
        val layout = panes(852f, 883f)

        assertEquals(listOf(DpBounds(0f, 0f, 360f, 883f), DpBounds(360f, 0f, 852f, 883f)), layout.panes)
    }

    @Test
    fun `list pane never grows past its maximum`() {
        for (width in 720..2400 step 13) {
            val list = panes(width.toFloat(), 1000f).panes.first().width
            assertTrue("width $width gives a $list list", list <= PaneLayout.LIST_MAX_WIDTH_DP)
        }
    }

    @Test
    fun `large flat window shows list, detail and history`() {
        val layout = panes(1376f, 1032f)

        assertEquals(3, layout.paneCount)
        assertEquals(listOf(385.28f, 630.72f, 360f), layout.panes.map { it.width })
    }

    @Test
    fun `at the large breakpoint the detail still gets its minimum`() {
        assertEquals(listOf(360f, 480f, 360f), panes(1200f, 900f).panes.map { it.width })
    }

    @Test
    fun `tri-fold gives one pane per segment`() {
        val layout = panes(1110f, 900f, hinge(x = 370f, height = 900f), hinge(x = 740f, height = 900f))

        assertEquals(listOf(370f, 370f, 370f), layout.panes.map { it.width })
    }

    @Test
    fun `tri-fold hinges reported in reverse still give panes left to right`() {
        val layout = panes(1110f, 900f, hinge(x = 740f, height = 900f), hinge(x = 370f, height = 900f))

        assertEquals(listOf(0f, 370f, 740f), layout.panes.map { it.left })
    }

    @Test
    fun `tri-fold with segments below the minimum collapses`() {
        assertEquals(1, panes(1020f, 900f, hinge(x = 340f, height = 900f), hinge(x = 680f, height = 900f)).paneCount)
    }

    @Test
    fun `book posture splits at the hinge, not at 40 or 50 percent`() {
        val layout = panes(852f, 883f, hinge(x = 400f, height = 883f))

        assertEquals(listOf(400f, 452f), layout.panes.map { it.width })
    }

    @Test
    fun `occluding hinge leaves a gap between the panes`() {
        val layout = panes(826f, 720f, hinge(x = 400f, height = 720f, width = 26f))

        assertEquals(400f, layout.panes[0].right)
        assertEquals(426f, layout.panes[1].left)
    }

    @Test
    fun `book posture with a pane below the minimum collapses rather than squeeze`() {
        assertEquals(1, panes(700f, 883f, hinge(x = 300f, height = 883f)).paneCount)
    }

    @Test
    fun `tabletop and cover keep their own single surface`() {
        assertEquals(1, panes(883f, 852f, hinge(x = 426f, height = 883f, vertical = false)).paneCount)
        assertEquals(1, panes(332f, 348f).paneCount)
    }
}
