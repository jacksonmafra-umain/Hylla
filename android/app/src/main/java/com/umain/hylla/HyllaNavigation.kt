package com.umain.hylla

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.layout.PaneLayout
import com.umain.hylla.layout.PaneRole
import com.umain.hylla.layout.MultiPaneSceneStrategy
import com.umain.hylla.posture.WindowPosture
import com.umain.hylla.ui.DeviceDetailScreen
import com.umain.hylla.ui.FleetScreen
import com.umain.hylla.ui.HistoryPane
import kotlinx.serialization.Serializable

@Serializable
data object FleetRoute : NavKey

@Serializable
data class DeviceRoute(val id: DeviceId) : NavKey

/**
 * Fleet, detail and history: one at a time, two side by side, or all three.
 *
 * The back stack is the same in both: `[Fleet]` or `[Fleet, Device]`. The scene strategy decides
 * from the [PaneLayout] whether the top entry is shown alone or next to the list. Folding or
 * resizing changes the layout, never the stack, so nothing is lost either way.
 */
@Composable
fun HyllaNavigation(fleet: Fleet, posture: WindowPosture, store: FleetStore) {
    val backStack = rememberNavBackStack(FleetRoute)
    val layout = remember(posture) { PaneLayout.compute(posture) }
    val strategy = remember(layout, fleet) {
        MultiPaneSceneStrategy<NavKey>(
            layout = layout,
            placeholder = { SelectDevicePlaceholder() },
            supporting = { subject -> HistoryPane(fleet, subject as? DeviceId) },
        )
    }
    val selected = (backStack.lastOrNull() as? DeviceRoute)?.id

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(strategy),
        entryProvider = entryProvider {
            entry<FleetRoute>(metadata = PaneRole.List.metadata()) {
                FleetScreen(
                    fleet = fleet,
                    posture = posture,
                    selected = selected.takeIf { layout.paneCount > 1 },
                    onDeviceClick = { backStack.select(it) },
                )
            }
            entry<DeviceRoute>(metadata = { route: DeviceRoute -> PaneRole.Detail.metadata(subject = route.id) }) { route ->
                DeviceDetailScreen(
                    fleet = fleet,
                    id = route.id,
                    widthClass = posture.widthClass,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

/** Shows [id] as the detail, replacing the current one rather than stacking details. */
private fun NavBackStack<NavKey>.select(id: DeviceId) {
    val route = DeviceRoute(id)
    if (lastOrNull() is DeviceRoute) set(lastIndex, route) else add(route)
}

@Composable
private fun SelectDevicePlaceholder() {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.select_device),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
