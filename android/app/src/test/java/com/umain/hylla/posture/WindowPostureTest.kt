package com.umain.hylla.posture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowPostureTest {

    private fun verticalFold(
        x: Float,
        height: Float,
        width: Float = 0f,
        separating: Boolean = true,
        state: FoldState = FoldState.HalfOpened,
    ) = Fold(
        bounds = DpBounds(x, 0f, x + width, height),
        orientation = FoldOrientation.Vertical,
        state = state,
        isSeparating = separating,
        occlusion = if (width > 0f) FoldOcclusion.Full else FoldOcclusion.None,
    )

    private fun horizontalFold(y: Float, width: Float) = Fold(
        bounds = DpBounds(0f, y, width, y),
        orientation = FoldOrientation.Horizontal,
        state = FoldState.HalfOpened,
        isSeparating = true,
        occlusion = FoldOcclusion.None,
    )

    // Size classes

    @Test
    fun `width breakpoints are 600, 840 and 1200`() {
        val cases = mapOf(
            599f to WidthClass.Compact,
            600f to WidthClass.Medium,
            839f to WidthClass.Medium,
            840f to WidthClass.Expanded,
            1199f to WidthClass.Expanded,
            1200f to WidthClass.Large,
            1600f to WidthClass.Large,
        )
        for ((width, expected) in cases) {
            assertEquals("width $width", expected, WindowPosture.compute(width, 800f).widthClass)
        }
    }

    @Test
    fun `height breakpoints are 480 and 900`() {
        val cases = mapOf(
            479f to HeightClass.Compact,
            480f to HeightClass.Medium,
            899f to HeightClass.Medium,
            900f to HeightClass.Expanded,
        )
        for ((height, expected) in cases) {
            assertEquals("height $height", expected, WindowPosture.compute(700f, height).heightClass)
        }
    }

    // Flat

    @Test
    fun `phone portrait is compact and flat with one segment`() {
        val posture = WindowPosture.compute(411f, 891f)

        assertEquals(WidthClass.Compact, posture.widthClass)
        assertEquals(Posture.Flat, posture.posture)
        assertEquals(listOf(DpBounds(0f, 0f, 411f, 891f)), posture.segments)
    }

    @Test
    fun `phone landscape is expanded width with compact height, and not a cover`() {
        val posture = WindowPosture.compute(891f, 411f)

        assertEquals(WidthClass.Expanded, posture.widthClass)
        assertEquals(HeightClass.Compact, posture.heightClass)
        assertEquals(Posture.Flat, posture.posture)
    }

    @Test
    fun `unfolded foldable lying flat does not split`() {
        val fold = verticalFold(x = 336f, height = 841f, separating = false, state = FoldState.Flat)

        val posture = WindowPosture.compute(673f, 841f, listOf(fold))

        assertEquals(Posture.Flat, posture.posture)
        assertEquals(listOf(fold), posture.folds)
        assertTrue(posture.hinges.isEmpty())
        assertEquals(1, posture.segments.size)
    }

    @Test
    fun `flat fold with an occluding hinge still splits because it separates`() {
        val hinge = verticalFold(x = 400f, height = 720f, width = 26f, state = FoldState.Flat)

        val posture = WindowPosture.compute(826f, 720f, listOf(hinge))

        assertEquals(Posture.Book, posture.posture)
    }

    // Book

    @Test
    fun `book posture splits at the hinge, not at half`() {
        val posture = WindowPosture.compute(673f, 841f, listOf(verticalFold(x = 300f, height = 841f)))

        assertEquals(Posture.Book, posture.posture)
        assertEquals(
            listOf(DpBounds(0f, 0f, 300f, 841f), DpBounds(300f, 0f, 673f, 841f)),
            posture.segments,
        )
    }

    @Test
    fun `occluding hinge is excluded from both segments`() {
        val posture = WindowPosture.compute(826f, 720f, listOf(verticalFold(x = 400f, height = 720f, width = 26f)))

        assertEquals(
            listOf(DpBounds(0f, 0f, 400f, 720f), DpBounds(426f, 0f, 826f, 720f)),
            posture.segments,
        )
    }

    // Tabletop

    @Test
    fun `horizontal hinge is tabletop with content above and controls below`() {
        val posture = WindowPosture.compute(673f, 841f, listOf(horizontalFold(y = 420f, width = 673f)))

        assertEquals(Posture.Tabletop, posture.posture)
        assertEquals(
            listOf(DpBounds(0f, 0f, 673f, 420f), DpBounds(0f, 420f, 673f, 841f)),
            posture.segments,
        )
    }

    // Tri-fold

    @Test
    fun `tri-fold reports both hinges and three segments`() {
        val hinges = listOf(verticalFold(x = 340f, height = 900f), verticalFold(x = 680f, height = 900f))

        val posture = WindowPosture.compute(1020f, 900f, hinges)

        assertEquals(Posture.TriFold, posture.posture)
        assertEquals(2, posture.hinges.size)
        assertEquals(listOf(340f, 340f, 340f), posture.segments.map { it.width })
    }

    @Test
    fun `hinges reported out of order still produce segments in reading order`() {
        val hinges = listOf(verticalFold(x = 680f, height = 900f), verticalFold(x = 340f, height = 900f))

        val posture = WindowPosture.compute(1020f, 900f, hinges)

        assertEquals(listOf(0f, 340f, 680f), posture.segments.map { it.left })
    }

    @Test
    fun `only the separating folds of a tri-fold become hinges`() {
        val folds = listOf(
            verticalFold(x = 340f, height = 900f),
            verticalFold(x = 680f, height = 900f, separating = false, state = FoldState.Flat),
        )

        val posture = WindowPosture.compute(1020f, 900f, folds)

        assertEquals(Posture.Book, posture.posture)
        assertEquals(2, posture.segments.size)
        assertEquals(2, posture.folds.size)
    }

    @Test
    fun `crossing folds are refused rather than guessed`() {
        val folds = listOf(verticalFold(x = 400f, height = 800f), horizontalFold(y = 400f, width = 800f))

        val posture = WindowPosture.compute(800f, 800f, folds)

        assertEquals(Posture.Flat, posture.posture)
        assertEquals(1, posture.segments.size)
    }

    // Cover

    @Test
    fun `flip cover screen is a cover surface`() {
        assertEquals(Posture.Cover, WindowPosture.compute(332f, 348f).posture)
    }

    @Test
    fun `small split-screen window is a cover surface too, because the window is the only input`() {
        assertEquals(Posture.Cover, WindowPosture.compute(411f, 420f).posture)
    }

    @Test
    fun `narrow but tall window is not a cover surface`() {
        assertEquals(Posture.Flat, WindowPosture.compute(332f, 700f).posture)
    }
}
