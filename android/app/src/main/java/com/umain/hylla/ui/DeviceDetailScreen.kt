package com.umain.hylla.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    fleet: Fleet,
    id: DeviceId,
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                items(fields, key = { it.label }) { field ->
                    ListItem(
                        // One element per field, so a screen reader reads "Model, Galaxy Z Fold7".
                        modifier = Modifier.semantics(mergeDescendants = true) {},
                        overlineContent = { Text(stringResource(field.label)) },
                        headlineContent = { Text(field.value) },
                    )
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
