package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.fleet.Lifecycle
import com.umain.hylla.fleet.PersonId
import com.umain.hylla.fleet.isClaimable
import com.umain.hylla.layout.AdaptiveGrid
import com.umain.hylla.layout.AdaptiveLayout
import com.umain.hylla.layout.LocalPaneCount
import com.umain.hylla.posture.WidthClass
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    fleet: Fleet,
    id: DeviceId,
    widthClass: WidthClass,
    store: FleetStore,
    me: PersonId?,
    watched: Boolean,
    onToggleWatch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val device = fleet.devices.firstOrNull { it.id == id }
    var sheet by rememberSaveable { mutableStateOf(DetailSheet.None) }
    val feedback = LocalFeedback.current
    val undo = stringResource(R.string.undo)
    val claimedBy = stringResource(R.string.scan_claimed_by)
    val returned = stringResource(R.string.scan_returned)
    val saved = stringResource(R.string.feedback_saved)
    if (device != null) {
        when (sheet) {
            DetailSheet.None -> Unit
            DetailSheet.Claim -> ClaimSheet(device, fleet, me, onClaim = { person ->
                store.claim(device.id, person).onSuccess {
                    feedback.show(claimedBy.format(device.deviceName, fleet.person(person)?.name.orEmpty()), undo) { store.undo() }
                }
                sheet = DetailSheet.None
            }, onDismiss = { sheet = DetailSheet.None })
            DetailSheet.Return -> AlertDialog(
                onDismissRequest = { sheet = DetailSheet.None },
                title = { Text(stringResource(R.string.return_title, device.deviceName)) },
                text = { Text(stringResource(R.string.return_message, device.currentUser?.let(fleet::person)?.name.orEmpty())) },
                confirmButton = {
                    TextButton(onClick = {
                        store.returnDevice(device.id).onSuccess {
                            feedback.show(returned.format(device.deviceName), undo) { store.undo() }
                        }
                        sheet = DetailSheet.None
                    }) {
                        Text(stringResource(R.string.scan_return))
                    }
                },
                dismissButton = { TextButton(onClick = { sheet = DetailSheet.None }) { Text(stringResource(R.string.cancel)) } },
            )
            DetailSheet.Edit -> EditSheet(device, onSave = { edited ->
                store.update(edited).onSuccess { feedback.show(saved.format(edited.deviceName), undo) { store.undo() } }
            }, onDismiss = { sheet = DetailSheet.None })
        }
    }
    val title = device?.deviceName ?: stringResource(R.string.device_not_found_title)
    Scaffold(
        // The pane title changes with the device, so selecting another tile is announced.
        modifier = modifier.semantics { paneTitle = title },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    // Beside the list there is nothing to go up to; system back still clears the detail.
                    if (LocalPaneCount.current == 1) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                actions = {
                    if (device != null) {
                        TextButton(onClick = { sheet = DetailSheet.Edit }) { Text(stringResource(R.string.edit_action)) }
                    }
                },
            )
        },
    ) { padding ->
        if (device == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.device_not_found, id.value))
            }
        } else {
            val fields = deviceFields(device, fleet)
            // With three panes the history has its own; otherwise it ends the detail.
            val showHistory = LocalPaneCount.current < 3
            AdaptiveGrid(
                widthClass = widthClass,
                minColumnWidth = AdaptiveLayout.DETAIL_FIELD_MIN_WIDTH_DP,
                contentPadding = padding,
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "action", span = { GridItemSpan(maxLineSpan) }) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (device.assignmentStatus == AssignmentStatus.InUse) {
                            OutlinedButton(onClick = { sheet = DetailSheet.Return }) { Text(stringResource(R.string.scan_return)) }
                        } else if (device.lifecycle == Lifecycle.InUse) {
                            Button(onClick = { sheet = DetailSheet.Claim }) { Text(stringResource(R.string.scan_claim)) }
                        }
                        // Waiting makes sense only for a device someone cannot take right now.
                        if (!device.isClaimable && device.lifecycle == Lifecycle.InUse) {
                            OutlinedButton(onClick = onToggleWatch) {
                                Text(stringResource(if (watched) R.string.unwatch else R.string.watch))
                            }
                        }
                    }
                }
                items(fields, key = { it.label }) { field ->
                    // One node per field, so a screen reader reads "Model, Galaxy Z Fold7".
                    Column(Modifier.semantics(mergeDescendants = true) {}) {
                        Text(
                            stringResource(field.label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(field.value, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                if (showHistory) {
                    item(key = "history", span = { GridItemSpan(maxLineSpan) }) {
                        HistorySection(fleet, device.id)
                    }
                }
            }
        }
    }
}

/** Which modal the detail shows. One at a time, saved so it survives a fold. */
private enum class DetailSheet { None, Claim, Return, Edit }

private data class DeviceField(val label: Int, val value: String)

@Composable
private fun deviceFields(device: Device, fleet: Fleet): List<DeviceField> {
    val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val os = listOfNotNull(
        stringResource(R.string.os_version, stringResource(device.platform.label), device.osVersion),
        device.uiVersion,
    ).joinToString(" · ")
    return listOfNotNull(
        DeviceField(R.string.field_status, statusText(device, fleet)),
        DeviceField(R.string.field_since, device.since.format(dateFormat)),
        DeviceField(R.string.field_model, device.modelName),
        DeviceField(R.string.field_model_number, device.modelNumber),
        DeviceField(R.string.field_type, stringResource(device.deviceType.label)),
        DeviceField(R.string.field_os, os),
        DeviceField(R.string.field_home_use, stringResource(device.homeUse.label)),
        DeviceField(R.string.field_lifecycle, stringResource(device.lifecycle.label)),
        DeviceField(R.string.field_shelf_tag, device.id.value),
        device.notes?.let { DeviceField(R.string.field_notes, it) },
    )
}
