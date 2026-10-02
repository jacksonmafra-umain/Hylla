package com.umain.hylla.layout

import com.umain.hylla.layout.AdaptiveLayout.FLEET_TILE_MIN_WIDTH_DP
import com.umain.hylla.posture.WidthClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveLayoutTest {

    /** Fleet tile columns for a container [width] wide, as the fleet screen computes them. */
    private fun fleetColumns(width: Float, widthClass: WidthClass, textScale: Float = 1f): Int {
        val margin = AdaptiveLayout.margin(widthClass)
        val gutter = AdaptiveLayout.gutter(widthClass)
        return AdaptiveLayout.columns(width - 2 * margin, FLEET_TILE_MIN_WIDTH_DP, gutter, textScale)
    }

    @Test
    fun `margins and gutters widen past compact`() {
        assertEquals(16f, AdaptiveLayout.margin(WidthClass.Compact))
        assertEquals(24f, AdaptiveLayout.margin(WidthClass.Medium))
        assertEquals(12f, AdaptiveLayout.gutter(WidthClass.Compact))
        assertEquals(16f, AdaptiveLayout.gutter(WidthClass.Large))
    }

    @Test
    fun `phone portrait has one column`() {
        assertEquals(1, fleetColumns(411f, WidthClass.Compact))
    }

    @Test
    fun `phone landscape gains a column but stays one pane`() {
        assertEquals(2, fleetColumns(891f, WidthClass.Expanded))
    }

    @Test
    fun `unfolded foldable has two columns`() {
        assertEquals(2, fleetColumns(852f, WidthClass.Expanded))
    }

    @Test
    fun `tablet portrait and landscape gain columns`() {
        assertEquals(3, fleetColumns(1032f, WidthClass.Expanded))
        assertEquals(4, fleetColumns(1376f, WidthClass.Large))
    }

    @Test
    fun `columns never stretch past twice their minimum`() {
        for (width in 300..2400 step 7) {
            val margin = 24f
            val gutter = 16f
            val available = width - 2 * margin
            val columns = AdaptiveLayout.columns(available, FLEET_TILE_MIN_WIDTH_DP, gutter)
            val columnWidth = (available - gutter * (columns - 1)) / columns
            assertTrue("width $width gives $columnWidth", columnWidth < FLEET_TILE_MIN_WIDTH_DP * 2 + gutter)
        }
    }

    @Test
    fun `never fewer than one column, even below the minimum`() {
        assertEquals(1, AdaptiveLayout.columns(120f, FLEET_TILE_MIN_WIDTH_DP, 12f))
    }

    @Test
    fun `larger text needs wider columns`() {
        assertEquals(3, fleetColumns(1032f, WidthClass.Expanded, textScale = 1f))
        assertEquals(2, fleetColumns(1032f, WidthClass.Expanded, textScale = 1.3f))
        assertEquals(1, fleetColumns(1032f, WidthClass.Expanded, textScale = 2f))
    }

    @Test
    fun `text scale is capped at two`() {
        assertEquals(
            fleetColumns(1600f, WidthClass.Large, textScale = 2f),
            fleetColumns(1600f, WidthClass.Large, textScale = 3.2f),
        )
    }

    @Test
    fun `text smaller than default does not shrink columns`() {
        assertEquals(fleetColumns(1032f, WidthClass.Expanded), fleetColumns(1032f, WidthClass.Expanded, textScale = 0.85f))
    }
}
