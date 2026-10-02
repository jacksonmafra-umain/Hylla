package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.PersonId

/**
 * Quick claim, in a small sheet sized to its content: who takes it, and one button. Opens fully
 * at its own height, with no half-open step to drag through.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimSheet(device: Device, fleet: Fleet, me: PersonId?, onClaim: (PersonId) -> Unit, onDismiss: () -> Unit) {
    var person by rememberSaveable { mutableStateOf((me ?: fleet.people.first().id).value) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.claim_title, device.deviceName),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(stringResource(R.string.scan_claim_as), style = MaterialTheme.typography.titleSmall)
            PersonPicker(fleet.people, PersonId(person), onSelect = { person = it.value })
            Button(onClick = { onClaim(PersonId(person)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(stringResource(R.string.scan_claim))
            }
        }
    }
}
