package com.umain.hylla.ui

import androidx.compose.foundation.layout.Arrangement
import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.NotificationSettings
import com.umain.hylla.fleet.PersonId
import com.umain.hylla.permission.PermissionState
import com.umain.hylla.permission.rememberRuntimePermission

/** Who holds this phone, and what they hold. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouScreen(fleet: Fleet, me: PersonId?, onChooseMe: (PersonId) -> Unit, settings: NotificationSettings, modifier: Modifier = Modifier) {
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
            NotificationsSection(settings)
        }
    }
}

/**
 * The notification permission is asked for here, when the switch is turned on, never at launch:
 * the person has just said they want notifications, so the system dialog makes sense.
 */
@Composable
private fun NotificationsSection(settings: NotificationSettings) {
    val permission = rememberRuntimePermission(Manifest.permission.POST_NOTIFICATIONS)
    val wanted by settings.enabled.collectAsStateWithLifecycle()
    val granted = permission.state == PermissionState.Granted
    Text(
        stringResource(R.string.notifications_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp).semantics { heading() },
    )
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = wanted && granted, role = Role.Switch) { on ->
                settings.set(on)
                if (on && !granted) {
                    if (permission.state == PermissionState.Blocked) permission.openSettings() else permission.request()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.notifications_switch), Modifier.weight(1f))
        Switch(checked = wanted && granted, onCheckedChange = null)
    }
    Text(
        stringResource(if (permission.state == PermissionState.Blocked && wanted) R.string.notifications_blocked else R.string.notifications_explain),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
