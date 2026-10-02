package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.layout.AdaptiveGrid
import com.umain.hylla.layout.AdaptiveLayout
import com.umain.hylla.posture.PostureReadout
import com.umain.hylla.posture.WindowPosture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetScreen(
    fleet: Fleet,
    posture: WindowPosture,
    onDeviceClick: (DeviceId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fleet_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        AdaptiveGrid(
            widthClass = posture.widthClass,
            minColumnWidth = AdaptiveLayout.FLEET_TILE_MIN_WIDTH_DP,
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(fleet.devices, key = { it.id.value }) { device ->
                DeviceTile(device, fleet, onClick = { onDeviceClick(device.id) })
            }
            item(key = "window", span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(top = 16.dp)) {
                    Text(
                        text = stringResource(R.string.fleet_this_window),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp).semantics { heading() },
                    )
                    PostureReadout(posture)
                }
            }
        }
    }
}

/**
 * Status sits on its own line rather than beside the name: at 200% font scale a trailing label
 * squeezes the model name into a column one word wide.
 */
@Composable
private fun DeviceTile(device: Device, fleet: Fleet, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick) {
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
