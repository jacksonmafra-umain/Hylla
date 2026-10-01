package com.umain.hylla.posture

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import kotlin.math.roundToInt

/**
 * What the posture model sees. Only the posture line is a live region: the window size changes
 * continuously while resizing and announcing it would drown everything else.
 */
@Composable
fun PostureReadout(posture: WindowPosture, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val style = MaterialTheme.typography.bodyMedium
        Text(
            stringResource(R.string.posture_window, posture.widthDp.roundToInt(), posture.heightDp.roundToInt()),
            style = style,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(
                R.string.posture_size_classes,
                stringResource(posture.widthClass.label),
                stringResource(posture.heightClass.label),
            ),
            style = style,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.posture_kind, stringResource(posture.posture.label)),
            style = style,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Text(
            stringResource(
                R.string.posture_segments,
                posture.segments.joinToString(" | ") { "${it.width.roundToInt()} × ${it.height.roundToInt()}" },
            ),
            style = style,
            textAlign = TextAlign.Center,
        )
    }
}

private val WidthClass.label: Int
    @StringRes get() = when (this) {
        WidthClass.Compact -> R.string.size_class_compact
        WidthClass.Medium -> R.string.size_class_medium
        WidthClass.Expanded -> R.string.size_class_expanded
        WidthClass.Large -> R.string.size_class_large
    }

private val HeightClass.label: Int
    @StringRes get() = when (this) {
        HeightClass.Compact -> R.string.size_class_compact
        HeightClass.Medium -> R.string.size_class_medium
        HeightClass.Expanded -> R.string.size_class_expanded
    }

private val Posture.label: Int
    @StringRes get() = when (this) {
        Posture.Flat -> R.string.posture_flat
        Posture.Cover -> R.string.posture_cover
        Posture.Book -> R.string.posture_book
        Posture.Tabletop -> R.string.posture_tabletop
        Posture.TriFold -> R.string.posture_trifold
    }
