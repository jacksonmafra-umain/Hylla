package com.umain.hylla.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
        LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
            items(fleet.devices, key = { it.id.value }) { device ->
                DeviceRow(device, fleet, onClick = { onDeviceClick(device.id) })
                HorizontalDivider()
            }
            item(key = "window") {
                Text(
                    text = stringResource(R.string.fleet_this_window),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
                        .semantics { heading() },
                )
                PostureReadout(posture, Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp))
            }
        }
    }
}

/**
 * Status sits on its own line rather than in the trailing slot: at 200% font scale a trailing
 * label squeezes the model name into a column one word wide.
 */
@Composable
private fun DeviceRow(device: Device, fleet: Fleet, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(device.deviceName) },
        supportingContent = {
            Column {
                Text(
                    stringResource(
                        R.string.device_row_supporting,
                        device.modelName,
                        stringResource(device.platform.label),
                        device.osVersion,
                    ),
                )
                Text(
                    statusText(device, fleet),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    )
}

/** "Available", or "In use · Alva Berg" when someone holds it. */
@Composable
fun statusText(device: Device, fleet: Fleet): String {
    val status = stringResource(device.assignmentStatus.label)
    val holder = device.currentUser?.let(fleet::person)?.name ?: return status
    return stringResource(R.string.status_with_holder, status, holder)
}
