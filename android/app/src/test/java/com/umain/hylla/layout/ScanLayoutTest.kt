package com.umain.hylla.layout

import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.Fold
import com.umain.hylla.posture.FoldOcclusion
import com.umain.hylla.posture.FoldOrientation
import com.umain.hylla.posture.FoldState
import com.umain.hylla.posture.WindowPosture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanLayoutTest {

    private fun crease(y: Float, width: Float, height: Float = 0f) = Fold(
        bounds = DpBounds(0f, y, width, y + height),
        orientation = FoldOrientation.Horizontal,
        state = FoldState.HalfOpened,
        isSeparating = true,
        occlusion = if (height > 0f) FoldOcclusion.Full else FoldOcclusion.None,
    )

    private fun scan(width: Float, height: Float, vararg folds: Fold) =
        ScanLayout.compute(WindowPosture.compute(width, height, folds.toList()))

    @Test
    fun `tabletop puts the viewfinder above the crease and the controls below`() {
        val layout = scan(883f, 852f, crease(y = 426f, width = 883f))

        assertEquals(DpBounds(0f, 0f, 883f, 426f), layout.viewfinder)
        assertEquals(DpBounds(0f, 426f, 883f, 852f), layout.controls)
    }

    @Test
    fun `tabletop splits at the crease, not at the default share`() {
        val layout = scan(883f, 852f, crease(y = 500f, width = 883f))

        assertEquals(500f, layout.viewfinder.bottom)
    }

    @Test
    fun `an occluding crease is left empty`() {
        val layout = scan(720f, 826f, crease(y = 400f, width = 720f, height = 26f))

        assertEquals(400f, layout.viewfinder.bottom)
        assertEquals(426f, layout.controls.top)
    }

    @Test
    fun `flat portrait stacks the viewfinder over the controls`() {
        val layout = scan(411f, 891f)

        assertFalse(layout.sideBySide)
        assertEquals(891f * ScanLayout.VIEWFINDER_SHARE, layout.viewfinder.bottom)
    }

    @Test
    fun `phone landscape puts them side by side because the height is compact`() {
        assertTrue(scan(891f, 411f).sideBySide)
    }
}
