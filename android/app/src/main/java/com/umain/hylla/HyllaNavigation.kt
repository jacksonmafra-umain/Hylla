package com.umain.hylla

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.posture.WindowPosture
import com.umain.hylla.ui.DeviceDetailScreen
import com.umain.hylla.ui.FleetScreen
import kotlinx.serialization.Serializable

@Serializable
data object FleetRoute : NavKey

@Serializable
data class DeviceRoute(val id: DeviceId) : NavKey

/**
 * Compact navigation: one screen at a time, fleet then detail.
 *
 * The back stack is a saveable list owned here, so it survives a fold, a rotation and process
 * death. Navigation 3 handles system back and the predictive back gesture from it.
 */
@Composable
fun HyllaNavigation(fleet: Fleet, posture: WindowPosture) {
    val backStack = rememberNavBackStack(FleetRoute)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<FleetRoute> {
                FleetScreen(
                    fleet = fleet,
                    posture = posture,
                    onDeviceClick = { backStack.add(DeviceRoute(it)) },
                )
            }
            entry<DeviceRoute> { route ->
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
