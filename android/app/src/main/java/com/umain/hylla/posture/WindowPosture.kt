package com.umain.hylla.posture

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass

/**
 * Everything layout is allowed to know about the window: its size, its size classes and the
 * folds that cross it. Built from the window, never from the display or the orientation.
 *
 * Pure Kotlin on purpose, so every posture can be unit tested without a device.
 */
data class WindowPosture(
    val widthDp: Float,
    val heightDp: Float,
    val widthClass: WidthClass,
    val heightClass: HeightClass,
    val posture: Posture,
    /** Every fold the window reports, separating or not. */
    val folds: List<Fold>,
    /** The folds that split the layout. Always a list: a tri-fold reports two. */
    val hinges: List<Fold>,
    /** The window divided at each hinge, in reading order. One entry when nothing separates. */
    val segments: List<DpBounds>,
) {
    companion object {
        /** Below this width, with a compact height, the window is a cover surface. */
        const val COVER_MAX_WIDTH_DP = 480f

        fun compute(widthDp: Float, heightDp: Float, folds: List<Fold> = emptyList()): WindowPosture {
            val sizeClass = WindowSizeClass.BREAKPOINTS_V2.computeWindowSizeClass(widthDp, heightDp)
            val widthClass = WidthClass.of(sizeClass)
            val heightClass = HeightClass.of(sizeClass)
            val window = DpBounds(0f, 0f, widthDp, heightDp)

            // isSeparating, not state, decides whether a fold splits the layout. A flat fold
            // with an occluding hinge separates; a half-open fold always does.
            val separating = folds.filter { it.isSeparating }
            val orientations = separating.map { it.orientation }.toSet()

            val cover = widthDp < COVER_MAX_WIDTH_DP && heightClass == HeightClass.Compact
            val posture = when {
                cover -> Posture.Cover
                separating.isEmpty() -> Posture.Flat
                // No hardware reports crossing folds. Refuse to guess a split.
                orientations.size > 1 -> Posture.Flat
                orientations.single() == FoldOrientation.Horizontal -> Posture.Tabletop
                separating.size == 1 -> Posture.Book
                else -> Posture.TriFold
            }
            val hinges = if (posture == Posture.Flat || posture == Posture.Cover) emptyList() else separating
            return WindowPosture(
                widthDp = widthDp,
                heightDp = heightDp,
                widthClass = widthClass,
                heightClass = heightClass,
                posture = posture,
                folds = folds,
                hinges = hinges,
                segments = window.splitAt(hinges),
            )
        }
    }
}

enum class Posture {
    /** No separating fold: a phone, a tablet, an unfolded foldable lying flat. */
    Flat,

    /** A window too small for navigation: a flip phone's cover screen, a tiny split-screen. */
    Cover,

    /** One vertical separating fold: two panes side by side. */
    Book,

    /** Horizontal separating folds: content above, controls below. */
    Tabletop,

    /** Two or more vertical separating folds. */
    TriFold,
}

enum class WidthClass {
    Compact, Medium, Expanded, Large;

    companion object {
        fun of(sizeClass: WindowSizeClass): WidthClass = when {
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) -> Large
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> Expanded
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> Medium
            else -> Compact
        }
    }
}

enum class HeightClass {
    Compact, Medium, Expanded;

    companion object {
        fun of(sizeClass: WindowSizeClass): HeightClass = when {
            sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND) -> Expanded
            sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> Medium
            else -> Compact
        }
    }
}

/** A fold in window coordinates, in dp. Mirrors `FoldingFeature` without depending on it. */
data class Fold(
    val bounds: DpBounds,
    val orientation: FoldOrientation,
    val state: FoldState,
    val isSeparating: Boolean,
    val occlusion: FoldOcclusion,
)

enum class FoldOrientation { Vertical, Horizontal }

enum class FoldState { Flat, HalfOpened }

enum class FoldOcclusion { None, Full }

data class DpBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    /**
     * Splits at each hinge's bounds, so an occluding hinge is excluded from both sides and a
     * hinge off-centre gives unequal segments. All hinges must share one orientation.
     */
    fun splitAt(hinges: List<Fold>): List<DpBounds> {
        if (hinges.isEmpty()) return listOf(this)
        val vertical = hinges.first().orientation == FoldOrientation.Vertical
        val cuts = hinges
            .map { if (vertical) it.bounds.left to it.bounds.right else it.bounds.top to it.bounds.bottom }
            .sortedBy { it.first }
        val starts = listOf(if (vertical) left else top) + cuts.map { it.second }
        val ends = cuts.map { it.first } + (if (vertical) right else bottom)
        return starts.zip(ends)
            .filter { (start, end) -> end > start }
            .map { (start, end) ->
                if (vertical) copy(left = start, right = end) else copy(top = start, bottom = end)
            }
    }
}
