package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceType
import com.umain.hylla.fleet.HomeUse
import com.umain.hylla.fleet.Lifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Edit a record, in a full-height sheet: a form this long needs the whole window. Who holds the
 * device is not editable here; claiming and returning change that. Leaving with unsaved changes
 * asks first, whether by back, a drag or a tap outside.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(device: Device, onSave: (Device) -> Result<Unit>, onDismiss: () -> Unit, title: String? = null, saveLabel: String? = null) {
    var name by rememberSaveable { mutableStateOf(device.deviceName) }
    var type by rememberSaveable { mutableStateOf(device.deviceType) }
    var os by rememberSaveable { mutableStateOf(device.osVersion) }
    var ui by rememberSaveable { mutableStateOf(device.uiVersion.orEmpty()) }
    var homeUse by rememberSaveable { mutableStateOf(device.homeUse) }
    var lifecycle by rememberSaveable { mutableStateOf(device.lifecycle) }
    var since by rememberSaveable { mutableStateOf(device.since.toString()) }
    var notes by rememberSaveable { mutableStateOf(device.notes.orEmpty()) }
    var pickingDate by rememberSaveable { mutableStateOf(false) }
    var confirmingDiscard by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val edited = device.copy(
        deviceName = name.trim(),
        deviceType = type,
        osVersion = os.trim(),
        uiVersion = ui.trim().ifEmpty { null },
        homeUse = homeUse,
        lifecycle = lifecycle,
        since = LocalDate.parse(since),
        notes = notes.trim().ifEmpty { null },
    )
    val dirty = edited != device

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            if (value == SheetValue.Hidden && dirty) {
                confirmingDiscard = true
                false
            } else {
                true
            }
        },
    )

    ModalBottomSheet(
        onDismissRequest = { if (dirty) confirmingDiscard = true else onDismiss() },
        sheetState = sheetState,
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                title ?: stringResource(R.string.edit_title, device.deviceName),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.edit_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            TypeField(type) { type = it }
            OutlinedTextField(os, { os = it }, label = { Text(stringResource(R.string.edit_os_version)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(ui, { ui = it }, label = { Text(stringResource(R.string.edit_ui_version)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.field_home_use), style = MaterialTheme.typography.titleSmall)
            Segmented(HomeUse.entries, homeUse, { stringResource(it.label) }) { homeUse = it }
            Text(stringResource(R.string.field_lifecycle), style = MaterialTheme.typography.titleSmall)
            Segmented(Lifecycle.entries, lifecycle, { stringResource(it.label) }) { lifecycle = it }
            OutlinedButton(onClick = { pickingDate = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.edit_since, LocalDate.parse(since).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))))
            }
            OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.field_notes)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { if (dirty) confirmingDiscard = true else onDismiss() }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { onSave(edited).onSuccess { onDismiss() }.onFailure { error = it.message } },
                    // A draft can be saved as it is; an edit only once something changed.
                    enabled = (dirty || saveLabel != null) && name.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(saveLabel ?: stringResource(R.string.save))
                }
            }
        }
    }

    if (pickingDate) {
        SincePicker(LocalDate.parse(since), onPick = { since = it.toString(); pickingDate = false }, onDismiss = { pickingDate = false })
    }
    if (confirmingDiscard) {
        AlertDialog(
            onDismissRequest = { confirmingDiscard = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_message)) },
            confirmButton = {
                TextButton(onClick = { confirmingDiscard = false; onDismiss() }) { Text(stringResource(R.string.discard)) }
            },
            dismissButton = { TextButton(onClick = { confirmingDiscard = false }) { Text(stringResource(R.string.keep_editing)) } },
        )
    }
}

/** The date picker works in UTC milliseconds; a calendar date is midnight UTC on that day. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SincePicker(initial: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val today = remember { LocalDate.now() }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis.toLocalDate() <= today
            override fun isSelectableYear(year: Int) = year <= today.year
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onPick(it.toLocalDate()) } ?: onDismiss() }) {
                Text(stringResource(R.string.done))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    ) {
        DatePicker(state)
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeField(selected: DeviceType, onSelect: (DeviceType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(selected.label),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_type)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DeviceType.entries.forEach { type ->
                DropdownMenuItem(text = { Text(stringResource(type.label)) }, onClick = { onSelect(type); expanded = false })
            }
        }
    }
}

@Composable
private fun <T> Segmented(options: List<T>, selected: T, label: @Composable (T) -> String, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label(option), maxLines = 1) }
        }
    }
}
