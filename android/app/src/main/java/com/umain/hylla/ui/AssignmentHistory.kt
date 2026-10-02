package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Assignment
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** The third pane: who held the selected device, newest first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPane(fleet: Fleet, device: DeviceId?, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.history_title)) }) },
    ) { padding ->
        val history = device?.let(fleet::history).orEmpty()
        if (device == null || history.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(if (device == null) R.string.history_select_device else R.string.history_empty),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = padding) {
                items(history, key = { "${it.person.value}-${it.from}" }) { HistoryRow(fleet, it) }
            }
        }
    }
}

/** History as a section of the detail, used when there is no room for a third pane. */
@Composable
fun HistorySection(fleet: Fleet, device: DeviceId, modifier: Modifier = Modifier) {
    val history = fleet.history(device)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            stringResource(R.string.history_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp).semantics { heading() },
        )
        if (history.isEmpty()) {
            Text(stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyMedium)
        }
        history.forEach { HistoryRow(fleet, it) }
    }
}

@Composable
private fun HistoryRow(fleet: Fleet, assignment: Assignment) {
    val format = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val from = assignment.from.format(format)
    val period = assignment.to?.let { stringResource(R.string.history_period, from, it.format(format)) }
        ?: stringResource(R.string.history_period_open, from)
    ListItem(
        headlineContent = { Text(fleet.person(assignment.person)?.name ?: assignment.person.value) },
        supportingContent = { Text(period) },
    )
}
