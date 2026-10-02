package com.umain.hylla

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetFilter
import com.umain.hylla.fleet.PersonId
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.fleet.MeStore
import com.umain.hylla.fleet.NotificationSettings
import com.umain.hylla.notify.WatchList
import com.umain.hylla.layout.ChromeKind
import com.umain.hylla.layout.ChromeLayout
import com.umain.hylla.layout.MultiPaneSceneStrategy
import com.umain.hylla.layout.PaneLayout
import com.umain.hylla.layout.PaneRole
import com.umain.hylla.layout.contentArea
import com.umain.hylla.posture.Posture
import com.umain.hylla.posture.WindowPosture
import com.umain.hylla.ui.CoverSurface
import com.umain.hylla.ui.DeviceDetailScreen
import com.umain.hylla.ui.Feedback
import com.umain.hylla.ui.FleetScreen
import com.umain.hylla.ui.LocalFeedback
import com.umain.hylla.ui.HistoryPane
import com.umain.hylla.ui.ScanScreen
import com.umain.hylla.ui.ThisDeviceScreen
import com.umain.hylla.ui.YouScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data object FleetRoute : NavKey

@Serializable
data class DeviceRoute(val id: DeviceId) : NavKey

@Serializable
data object ScanRoute : NavKey

/** Top-level destinations, the ones the navigation chrome switches between. */
enum class TopLevel(val label: Int, val icon: ImageVector) {
    Fleet(R.string.nav_fleet, Icons.AutoMirrored.Filled.List),
    ThisDevice(R.string.nav_this_device, Icons.Filled.Info),
    You(R.string.nav_you, Icons.Filled.Person),
}

/**
 * The app shell: navigation chrome around three top-level destinations.
 *
 * The fleet keeps its own back stack, `[Fleet]` or `[Fleet, Device]`, shown one pane at a time or
 * side by side by the scene strategy. The cover surface and the scanner take the whole window, so
 * they are drawn instead of the shell rather than inside it.
 */
@Composable
fun HyllaNavigation(
    fleet: Fleet,
    posture: WindowPosture,
    store: FleetStore,
    meStore: MeStore,
    notifications: NotificationSettings,
    watchList: WatchList,
    link: DeviceId? = null,
    onLinkOpened: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(FleetRoute)
    var tab by rememberSaveable { mutableStateOf(TopLevel.Fleet) }
    val me by meStore.me.collectAsStateWithLifecycle()

    // A device link replaces whatever was open with [Fleet, Device]: back from it goes to the
    // fleet, never to the screen that happened to be open before the link arrived.
    LaunchedEffect(link) {
        if (link == null) return@LaunchedEffect
        tab = TopLevel.Fleet
        backStack.clear()
        backStack.add(FleetRoute)
        backStack.add(DeviceRoute(link))
        onLinkOpened()
    }

    // The cover surface replaces the whole UI without touching the back stack, so opening the
    // phone again lands exactly where you were.
    if (posture.posture == Posture.Cover && backStack.lastOrNull() != ScanRoute) {
        CoverSurface(fleet, me, onScan = { backStack.add(ScanRoute) })
        return
    }
    if (backStack.lastOrNull() == ScanRoute) {
        ScanScreen(fleet, posture, store, me, onBack = { backStack.removeLastOrNull() })
        return
    }

    val chrome = ChromeLayout.compute(posture)
    // Back from another top-level destination returns to the fleet before it leaves the app.
    BackHandler(enabled = tab != TopLevel.Fleet) { tab = TopLevel.Fleet }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val feedback = remember(snackbar, scope) { Feedback(snackbar, scope) }

    CompositionLocalProvider(LocalFeedback provides feedback) {
    NavigationSuiteScaffold(
        // The rail and the panes handle the system bars, but not a camera cutout on a side edge:
        // a Fold's inner camera sits on the left edge in portrait, right where the rail's items
        // are. Keep the whole shell clear of side cutouts and waterfall edges.
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.displayCutout.union(WindowInsets.waterfall).only(WindowInsetsSides.Horizontal),
        ),
        navigationSuiteType = chrome.kind.suiteType,
        // Centred in a rail or drawer rather than packed at the top. It reads well on tall
        // windows, and it keeps the items clear of a camera the device does not report: the
        // SM-F971B's inner display declares no cutout, so no inset can move anything off it.
        navigationItemVerticalArrangement = Arrangement.Center,
        navigationItems = {
            TopLevel.entries.forEach { destination ->
                NavigationSuiteItem(
                    selected = tab == destination,
                    onClick = { tab = destination },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(stringResource(destination.label)) },
                    navigationSuiteType = chrome.kind.suiteType,
                )
            }
        },
    ) {
        // One snackbar host for the whole shell, inside the chrome, so a snackbar never covers
        // the navigation bar and stays put when a pane changes underneath it.
        Box(Modifier.fillMaxSize()) {
            when (tab) {
                TopLevel.Fleet -> FleetPanes(fleet, posture.contentArea(chrome), backStack, store, me, watchList)
                TopLevel.ThisDevice -> ThisDeviceScreen(posture, fleet, store)
                TopLevel.You -> YouScreen(fleet, me, onChooseMe = meStore::set, settings = notifications)
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
        }
    }
    }
}

private val ChromeKind.suiteType: NavigationSuiteType
    get() = when (this) {
        ChromeKind.None -> NavigationSuiteType.None
        ChromeKind.BottomBar -> NavigationSuiteType.NavigationBar
        ChromeKind.Rail -> NavigationSuiteType.NavigationRail
        ChromeKind.Drawer -> NavigationSuiteType.NavigationDrawer
    }

/** Fleet, detail and history: one at a time, two side by side, or all three. */
@Composable
private fun FleetPanes(fleet: Fleet, content: WindowPosture, backStack: NavBackStack<NavKey>, store: FleetStore, me: PersonId?, watchList: WatchList) {
    val watched by watchList.watched.collectAsStateWithLifecycle()
    var filter by rememberSaveable(stateSaver = FleetFilterSaver) { mutableStateOf(FleetFilter()) }
    val layout = remember(content) { PaneLayout.compute(content) }
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
        // During the predictive back gesture the scene underneath grows into place while the
        // current one shrinks and fades, so letting go is a decision made with the destination
        // already in view. Folding mid-gesture is fine: the back stack has one shape everywhere.
        predictivePopTransitionSpec = {
            ContentTransform(
                targetContentEnter = fadeIn() + scaleIn(initialScale = 0.92f),
                initialContentExit = fadeOut() + scaleOut(targetScale = 0.92f),
            )
        },
        entryProvider = entryProvider {
            entry<FleetRoute>(metadata = PaneRole.List.metadata()) {
                FleetScreen(
                    fleet = fleet,
                    widthClass = content.widthClass,
                    selected = selected.takeIf { layout.paneCount > 1 },
                    onDeviceClick = { backStack.select(it) },
                    onScan = { backStack.add(ScanRoute) },
                    filter = filter,
                    onFilterChange = { filter = it },
                )
            }
            entry<DeviceRoute>(metadata = { route: DeviceRoute -> PaneRole.Detail.metadata(subject = route.id) }) { route ->
                DeviceDetailScreen(
                    fleet = fleet,
                    id = route.id,
                    widthClass = content.widthClass,
                    store = store,
                    me = me,
                    watched = route.id in watched,
                    onToggleWatch = { watchList.toggle(route.id) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}

/** The filter survives process death as JSON in the saved state. */
private val FleetFilterSaver = Saver<FleetFilter, String>(
    save = { Json.encodeToString(it) },
    restore = { Json.decodeFromString(it) },
)

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
