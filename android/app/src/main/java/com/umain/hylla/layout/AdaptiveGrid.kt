package com.umain.hylla.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.umain.hylla.posture.WidthClass

/**
 * A vertical grid whose column count comes from its own width.
 *
 * [contentPadding] is the Scaffold's inset padding; the adaptive margin is added on top of it,
 * and the columns are fitted to what is left. `GridCells.Adaptive` uses the same formula, but
 * computing the count with [AdaptiveLayout.columns] keeps it unit tested and identical to iOS.
 */
@Composable
fun AdaptiveGrid(
    widthClass: WidthClass,
    minColumnWidth: Float,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: LazyGridScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val textScale = LocalDensity.current.fontScale
    val margin = AdaptiveLayout.margin(widthClass).dp
    val gutter = AdaptiveLayout.gutter(widthClass).dp
    BoxWithConstraints(modifier) {
        val start = contentPadding.calculateStartPadding(direction) + margin
        val end = contentPadding.calculateEndPadding(direction) + margin
        val columns = AdaptiveLayout.columns(
            availableWidth = (maxWidth - start - end).value,
            minColumnWidth = minColumnWidth,
            gutter = gutter.value,
            textScale = textScale,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(
                start = start,
                top = contentPadding.calculateTopPadding() + gutter,
                end = end,
                bottom = contentPadding.calculateBottomPadding() + margin,
            ),
            horizontalArrangement = Arrangement.spacedBy(gutter),
            verticalArrangement = Arrangement.spacedBy(gutter),
            content = content,
        )
    }
}
