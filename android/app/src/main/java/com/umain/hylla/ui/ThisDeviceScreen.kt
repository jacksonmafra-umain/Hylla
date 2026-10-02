package com.umain.hylla.ui

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.posture.PostureReadout
import com.umain.hylla.posture.WindowPosture
import com.umain.hylla.register.BuildInfo
import com.umain.hylla.register.Registration
import java.time.LocalDate

/** What the app knows about the device it runs on, and registering it in the fleet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThisDeviceScreen(posture: WindowPosture, fleet: Fleet, store: FleetStore, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val feedback = LocalFeedback.current
    var registering by rememberSaveable { mutableStateOf(false) }
    val registered = stringResource(R.string.register_done)
    if (registering) {
        val build = BuildInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            release = Build.VERSION.RELEASE,
            deviceName = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME),
        )
        // Drafted from the window as it is right now: unfold first to record the inner display.
        val draft = Registration.draft(build, posture, fleet, LocalDate.now())
        EditSheet(
            device = draft,
            onSave = { device -> store.register(device).onSuccess { feedback.show(registered.format(device.deviceName, device.id.value)) } },
            onDismiss = { registering = false },
            title = stringResource(R.string.register_title),
            saveLabel = stringResource(R.string.register_save),
        )
    }
    Scaffold(modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_this_device)) }) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PostureReadout(posture)
            Text(stringResource(R.string.register_explain), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { registering = true }) { Text(stringResource(R.string.register_action)) }
        }
    }
}
