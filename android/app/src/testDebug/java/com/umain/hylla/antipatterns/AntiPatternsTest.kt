package com.umain.hylla.antipatterns

import com.umain.hylla.layout.PaneLayout
import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.Fold
import com.umain.hylla.posture.FoldOcclusion
import com.umain.hylla.posture.FoldOrientation
import com.umain.hylla.posture.FoldState
import com.umain.hylla.posture.WindowPosture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Each test shows the wrong rule producing the wrong layout, next to the right rule on the same
 * input. They pass because the bug is real: if one ever fails, the anti-pattern stopped being one.
 */
class AntiPatternsTest {

    private fun panes(width: Float, height: Float, folds: List<Fold> = emptyList()) =
        PaneLayout.compute(WindowPosture.compute(width, height, folds)).paneCount

    @Test
    fun `orientation is not size - a phone on its side is not a tablet`() {
        // 891 × 411: landscape, so the wrong rule splits it into two cramped panes.
        assertEquals(2, AntiPatterns.panesByOrientation(891f, 411f))
        assertEquals(1, panes(891f, 411f))
    }

    @Test
    fun `orientation is not size - a tablet held upright has room for two`() {
        // 1032 × 1376: portrait, so the wrong rule gives one stretched pane.
        assertEquals(1, AntiPatterns.panesByOrientation(1032f, 1376f))
        assertEquals(2, panes(1032f, 1376f))
    }

    @Test
    fun `the window is not the display - split screen on a large display`() {
        // The app has the left 420 dp of a 1376 dp display. Laid out for the display, it would
        // put three panes in a window that holds one.
        assertEquals(3, AntiPatterns.panesByDisplay(displayWidthDp = 1376f, windowHeightDp = 900f))
        assertEquals(1, panes(420f, 900f))
    }

    @Test
    fun `one hinge is an assumption - a tri-fold's second hinge runs through a pane`() {
        val hinges = listOf(370f, 740f).map {
            Fold(DpBounds(it, 0f, it, 900f), FoldOrientation.Vertical, FoldState.HalfOpened, true, FoldOcclusion.None)
        }
        val window = DpBounds(0f, 0f, 1110f, 900f)

        val wrong = AntiPatterns.segmentsFromFirstHinge(window, hinges)
        assertEquals(2, wrong.size)
        // The second pane spans 370–1110, so the crease at 740 cuts through its content.
        assertTrue(wrong[1].left < 740f && 740f < wrong[1].right)

        assertEquals(listOf(370f, 370f, 370f), WindowPosture.compute(1110f, 900f, hinges).segments.map { it.width })
    }
}
