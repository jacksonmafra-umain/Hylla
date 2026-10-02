package com.umain.hylla.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.umain.hylla.fleet.FleetFilter
import com.umain.hylla.fleet.loadFleet
import com.umain.hylla.posture.WindowPosture
import com.umain.hylla.ui.theme.HyllaTheme

/**
 * The fleet at every reference size and at every font scale, from the fixture.
 *
 * A preview has no activity, so the posture comes from the preview's window size through the same
 * [WindowPosture.compute] the app uses. Previews show size and text; folds, rotation and the real
 * window need the emulator (chapter 20).
 */
@Composable
private fun FleetPreview() {
    val size = LocalWindowInfo.current.containerSize
    val posture = with(LocalDensity.current) { WindowPosture.compute(size.width.toDp().value, size.height.toDp().value) }
    HyllaTheme {
        FleetScreen(
            fleet = LocalContext.current.assets.loadFleet(),
            widthClass = posture.widthClass,
            selected = null,
            onDeviceClick = {},
            onScan = {},
            filter = FleetFilter(),
            onFilterChange = {},
        )
    }
}

@PreviewScreenSizes
@Composable
private fun FleetAtEverySizePreview() = FleetPreview()

@PreviewFontScale
@Preview(name = "Foldable, 200%", device = "spec:width=673dp,height=841dp", fontScale = 2f)
@Composable
private fun FleetAtEveryFontScalePreview() = FleetPreview()
