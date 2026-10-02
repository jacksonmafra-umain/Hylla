package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.PersonId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * The cover display: you are standing at the shelf with the phone shut.
 *
 * One action, scan to check out, and the devices you hold right now. No navigation chrome: there
 * is no room for it at ~350 dp, and nothing else is worth doing with the phone closed.
 */
@Composable
fun CoverSurface(fleet: Fleet, me: PersonId?, onScan: () -> Unit, modifier: Modifier = Modifier) {
    val held = fleet.devices.filter { me != null && it.currentUser == me }
    val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    Surface(modifier.fillMaxSize()) {
        Column(
            Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = onScan, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
                Text(stringResource(R.string.cover_scan), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                stringResource(R.string.cover_you_hold),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.semantics { heading() },
            )
            when {
                me == null -> Text(stringResource(R.string.cover_choose_me), textAlign = TextAlign.Center)
                held.isEmpty() -> Text(stringResource(R.string.cover_nothing_held), textAlign = TextAlign.Center)
                else -> held.forEach { device ->
                    Text(
                        stringResource(R.string.cover_held_device, device.deviceName, device.since.format(format)),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
