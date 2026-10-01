package com.umain.hylla.posture

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetrics
import androidx.window.layout.WindowMetricsCalculator

/**
 * The current [WindowPosture], updated when the window resizes or a fold changes.
 *
 * Size comes from [WindowMetricsCalculator], never `Resources.displayMetrics` (the display, not
 * the window) or `LocalConfiguration.screenWidthDp` (rounded, and excludes insets). The
 * configuration is read only as a signal that the window may have changed.
 */
@Composable
fun rememberWindowPosture(): WindowPosture {
    val activity = checkNotNull(LocalActivity.current) { "WindowPosture needs an Activity" }
    val configuration = LocalConfiguration.current
    val metrics = remember(activity, configuration) {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity)
    }
    val displayFeatures by remember(activity) {
        WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).map { it.displayFeatures }
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    return remember(metrics, displayFeatures) { windowPosture(metrics, displayFeatures) }
}

fun windowPosture(metrics: WindowMetrics, displayFeatures: List<DisplayFeature>): WindowPosture {
    // Every folding feature, not the first one: a tri-fold reports two.
    val folds = displayFeatures
        .filterIsInstance<FoldingFeature>()
        .map { it.toFold(metrics.density) }
    return WindowPosture.compute(metrics.widthDp, metrics.heightDp, folds)
}

private fun FoldingFeature.toFold(density: Float) = Fold(
    // Bounds are already relative to the window, in pixels.
    bounds = DpBounds(
        left = bounds.left / density,
        top = bounds.top / density,
        right = bounds.right / density,
        bottom = bounds.bottom / density,
    ),
    orientation = when (orientation) {
        FoldingFeature.Orientation.HORIZONTAL -> FoldOrientation.Horizontal
        else -> FoldOrientation.Vertical
    },
    state = when (state) {
        FoldingFeature.State.HALF_OPENED -> FoldState.HalfOpened
        else -> FoldState.Flat
    },
    isSeparating = isSeparating,
    occlusion = when (occlusionType) {
        FoldingFeature.OcclusionType.FULL -> FoldOcclusion.Full
        else -> FoldOcclusion.None
    },
)
