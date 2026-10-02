package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.PersonId

/** Who holds this phone, and what they hold. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouScreen(fleet: Fleet, me: PersonId?, onChooseMe: (PersonId) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_you)) }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.me_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            PersonPicker(fleet.people, me, onSelect = onChooseMe)
            Text(
                stringResource(R.string.cover_you_hold),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp).semantics { heading() },
            )
            val held = fleet.devices.filter { me != null && it.currentUser == me }
            if (held.isEmpty()) Text(stringResource(R.string.cover_nothing_held))
            held.forEach { Text(it.deviceName, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
