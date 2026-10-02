package com.umain.hylla.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetFilter
import com.umain.hylla.layout.AdaptiveGrid
import com.umain.hylla.layout.AdaptiveLayout
import com.umain.hylla.posture.WidthClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen(
    fleet: Fleet,
    widthClass: WidthClass,
    selected: DeviceId?,
    onDeviceClick: (DeviceId) -> Unit,
    onScan: () -> Unit,
    filter: FleetFilter,
    onFilterChange: (FleetFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filtering by rememberSaveable { mutableStateOf(false) }
    if (filtering) FilterSheet(filter, onFilterChange, onDismiss = { filtering = false })
    val devices = fleet.devices.filter(filter::matches)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fleet_title)) },
                actions = {
                    TextButton(onClick = { filtering = true }) {
                        Text(
                            if (filter.activeCount == 0) stringResource(R.string.filter_action)
                            else stringResource(R.string.filter_action_count, filter.activeCount),
                        )
                    }
                    TextButton(onClick = onScan) { Text(stringResource(R.string.scan_action)) }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        AdaptiveGrid(
            widthClass = widthClass,
            minColumnWidth = AdaptiveLayout.FLEET_TILE_MIN_WIDTH_DP,
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (devices.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    Text(stringResource(R.string.filter_no_match), Modifier.padding(vertical = 24.dp))
                }
            }
            items(devices, key = { it.id.value }) { device ->
                DeviceTile(device, fleet, selected = device.id == selected, onClick = { onDeviceClick(device.id) })
            }
        }
    }
}

/**
 * Status sits on its own line rather than beside the name: at 200% font scale a trailing label
 * squeezes the model name into a column one word wide.
 */
@Composable
private fun DeviceTile(device: Device, fleet: Fleet, selected: Boolean, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.semantics { this.selected = selected },
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder(),
        colors = if (selected) {
            CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.outlinedCardColors()
        },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(device.deviceName, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.device_row_supporting,
                    device.modelName,
                    stringResource(device.platform.label),
                    device.osVersion,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(statusText(device, fleet), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** "Available", or "In use · Alva Berg" when someone holds it. */
@Composable
fun statusText(device: Device, fleet: Fleet): String {
    val status = stringResource(device.assignmentStatus.label)
    val holder = device.currentUser?.let(fleet::person)?.name ?: return status
    return stringResource(R.string.status_with_holder, status, holder)
}
