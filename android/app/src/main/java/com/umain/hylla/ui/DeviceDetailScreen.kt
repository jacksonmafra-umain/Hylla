package com.umain.hylla.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import com.umain.hylla.R
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.layout.AdaptiveGrid
import com.umain.hylla.layout.AdaptiveLayout
import com.umain.hylla.layout.LocalInMultiPane
import com.umain.hylla.posture.WidthClass
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    fleet: Fleet,
    id: DeviceId,
    widthClass: WidthClass,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val device = fleet.devices.firstOrNull { it.id == id }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(device?.deviceName ?: stringResource(R.string.device_not_found_title)) },
                navigationIcon = {
                    // Beside the list there is nothing to go up to; system back still clears the detail.
                    if (!LocalInMultiPane.current) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
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
            AdaptiveGrid(
                widthClass = widthClass,
                minColumnWidth = AdaptiveLayout.DETAIL_FIELD_MIN_WIDTH_DP,
                contentPadding = padding,
                modifier = Modifier.fillMaxSize(),
            ) {
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
            }
        }
    }
}

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
