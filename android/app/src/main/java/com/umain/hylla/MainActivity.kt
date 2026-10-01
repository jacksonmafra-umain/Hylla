package com.umain.hylla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.Lifecycle
import com.umain.hylla.fleet.loadFleet
import com.umain.hylla.posture.PostureReadout
import com.umain.hylla.posture.WindowPosture
import com.umain.hylla.posture.rememberWindowPosture

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HyllaApp()
            }
        }
    }
}

@Composable
fun HyllaApp() {
    val assets = LocalContext.current.assets
    val fleet = remember(assets) { assets.loadFleet() }
    val posture = rememberWindowPosture()
    FleetSummary(fleet, posture)
}

@Composable
private fun FleetSummary(fleet: Fleet, posture: WindowPosture, modifier: Modifier = Modifier) {
    val available = fleet.devices.count {
        it.assignmentStatus == AssignmentStatus.Available && it.lifecycle == Lifecycle.InUse
    }
    Surface(modifier.fillMaxSize()) {
        // Centred when it fits; scrolls at large font scales instead of clipping.
        Box(Modifier.safeDrawingPadding(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = pluralStringResource(R.plurals.fleet_summary_devices, fleet.devices.size, fleet.devices.size),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = pluralStringResource(R.plurals.fleet_summary_available, available, available),
                    style = MaterialTheme.typography.bodyLarge,
                )
                PostureReadout(posture, Modifier.padding(top = 16.dp))
            }
        }
    }
}
