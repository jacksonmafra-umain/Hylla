package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.DeviceType
import com.umain.hylla.fleet.FleetFilter
import com.umain.hylla.fleet.Platform

/**
 * Filters, in a sheet that opens at half height: enough to see the list change behind it, and
 * draggable to full height when the text is large. Changes apply as they are made.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(filter: FleetFilter, onChange: (FleetFilter) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                TextButton(onClick = { onChange(FleetFilter()) }, enabled = filter.activeCount > 0) {
                    Text(stringResource(R.string.filter_clear))
                }
            }
            Section(R.string.filter_platform)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Platform.entries.forEach { platform ->
                    FilterChip(
                        selected = platform in filter.platforms,
                        onClick = { onChange(filter.copy(platforms = filter.platforms.toggle(platform))) },
                        label = { Text(stringResource(platform.label)) },
                    )
                }
            }
            Section(R.string.filter_type)
            val types = listOf<DeviceType?>(null) + DeviceType.entries
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                types.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = filter.type == type,
                        onClick = { onChange(filter.copy(type = type)) },
                        shape = SegmentedButtonDefaults.itemShape(index, types.size),
                    ) {
                        Text(stringResource(type?.label ?: R.string.filter_any), maxLines = 1)
                    }
                }
            }
            Section(R.string.filter_status)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssignmentStatus.entries.forEach { status ->
                    FilterChip(
                        selected = status in filter.statuses,
                        onClick = { onChange(filter.copy(statuses = filter.statuses.toggle(status))) },
                        label = { Text(stringResource(status.label)) },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.filter_claimable), Modifier.weight(1f))
                Switch(checked = filter.claimableOnly, onCheckedChange = { onChange(filter.copy(claimableOnly = it)) })
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(stringResource(R.string.done))
            }
        }
    }
}

@Composable
private fun Section(label: Int) {
    Text(stringResource(label), style = MaterialTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
