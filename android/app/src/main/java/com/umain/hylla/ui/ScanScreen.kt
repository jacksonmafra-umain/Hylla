package com.umain.hylla.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umain.hylla.R
import com.umain.hylla.fleet.AssignmentStatus
import com.umain.hylla.fleet.Device
import com.umain.hylla.fleet.DeviceId
import com.umain.hylla.fleet.Fleet
import com.umain.hylla.fleet.FleetStore
import com.umain.hylla.fleet.PersonId
import com.umain.hylla.fleet.ShelfTag
import com.umain.hylla.fleet.device
import com.umain.hylla.layout.ScanLayout
import com.umain.hylla.posture.DpBounds
import com.umain.hylla.posture.Posture
import com.umain.hylla.posture.WindowPosture

/**
 * Check a device out or back in at the shelf.
 *
 * The viewfinder and the controls are placed at the bounds [ScanLayout] computes, so in tabletop
 * the crease runs between them. The camera itself arrives with the permission flow in chapter 14;
 * typing the tag works everywhere and stays as the accessible fallback.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanScreen(fleet: Fleet, posture: WindowPosture, store: FleetStore, me: PersonId?, onBack: () -> Unit) {
    val computed = remember(posture) { ScanLayout.compute(posture) }
    // Typing a tag in a stacked layout: the keyboard rises into the controls, and on a wide
    // window held upright (an unfolded Fold in landscape) only the field was left visible. Shrink
    // the viewfinder to a strip while the keyboard is up. Tabletop keeps its split at the crease.
    val typing = WindowInsets.isImeVisible && !computed.sideBySide && posture.posture != Posture.Tabletop
    val layout = if (typing) computed.withViewfinderHeight(KEYBOARD_VIEWFINDER_DP) else computed
    // Drawn outside the navigation display, so it handles system back itself.
    BackHandler(onBack = onBack)
    Box(Modifier.fillMaxSize()) {
        Viewfinder(Modifier.placeAt(layout.viewfinder), onBack)
        Surface(Modifier.placeAt(layout.controls)) {
            ScanControls(fleet, store, me)
        }
    }
}

private const val KEYBOARD_VIEWFINDER_DP = 96f

private fun ScanLayout.withViewfinderHeight(height: Float): ScanLayout {
    val split = viewfinder.top + minOf(height, viewfinder.height)
    return copy(viewfinder = viewfinder.copy(bottom = split), controls = controls.copy(top = split))
}

private fun Modifier.placeAt(bounds: DpBounds) =
    offset(bounds.left.dp, bounds.top.dp).size(bounds.width.dp, bounds.height.dp)

/**
 * The aiming guide scales with the viewfinder: on a cover screen the viewfinder is ~150 dp tall
 * and a fixed 200 dp guide would run over everything. The hint only shows where it fits.
 */
@Composable
private fun Viewfinder(modifier: Modifier, onBack: () -> Unit) {
    val description = stringResource(R.string.scan_viewfinder)
    BoxWithConstraints(modifier.background(Color(0xFF101418))) {
        val guide = (minOf(maxWidth, maxHeight) * 0.55f).coerceAtMost(200.dp)
        Box(
            Modifier
                .align(Alignment.Center)
                .size(guide)
                .border(3.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(guide / 8))
                .semantics { contentDescription = description },
        )
        if (maxHeight >= 280.dp) {
            Text(
                stringResource(R.string.scan_hint),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
        }
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
            modifier = Modifier.statusBarsPadding().padding(4.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
        }
    }
}

@Composable
private fun ScanControls(fleet: Fleet, store: FleetStore, me: PersonId?) {
    var tag by rememberSaveable { mutableStateOf("") }
    var found by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    val device = found?.let { fleet.device(DeviceId(it)) }
    val notATag = stringResource(R.string.scan_not_a_tag)
    val keyboard = LocalSoftwareKeyboardController.current

    fun lookUp() {
        // In tabletop the keyboard fills the controls segment; put it away to show the result.
        keyboard?.hide()
        val id = ShelfTag.parse(tag)
        found = id?.value
        message = if (id == null) notATag else null
    }

    Column(
        Modifier
            .fillMaxSize()
            // The keyboard rises into the controls segment; shrink the scroll viewport above it so
            // the field and its buttons can scroll into view instead of hiding under it.
            .imePadding()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.scan_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        OutlinedTextField(
            value = tag,
            onValueChange = { tag = it },
            label = { Text(stringResource(R.string.scan_tag_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { lookUp() }),
            modifier = Modifier.fillMaxWidth(),
        )
        FilledTonalButton(onClick = ::lookUp, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.scan_look_up))
        }
        when {
            device != null -> FoundDevice(device, fleet, store, me) { message = it }
            found != null -> Text(stringResource(R.string.device_not_found, found!!))
        }
        message?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
    }
}

@Composable
private fun FoundDevice(device: Device, fleet: Fleet, store: FleetStore, me: PersonId?, onDone: (String) -> Unit) {
    val claimedBy = stringResource(R.string.scan_claimed_by)
    val returned = stringResource(R.string.scan_returned)
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(device.deviceName, style = MaterialTheme.typography.titleMedium)
            Text(statusText(device, fleet), style = MaterialTheme.typography.labelLarge)
            if (device.assignmentStatus == AssignmentStatus.InUse) {
                Button(onClick = {
                    store.returnDevice(device.id).onSuccess { onDone(returned.format(device.deviceName)) }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_return))
                }
            } else {
                // Defaults to whoever holds this phone.
                var person by rememberSaveable { mutableStateOf((me ?: fleet.people.first().id).value) }
                PersonField(fleet, PersonId(person)) { person = it.value }
                Button(onClick = {
                    val name = fleet.person(PersonId(person))?.name.orEmpty()
                    store.claim(device.id, PersonId(person)).onSuccess { onDone(claimedBy.format(device.deviceName, name)) }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.scan_claim))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonField(fleet: Fleet, selected: PersonId, onSelect: (PersonId) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = fleet.person(selected)?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.scan_claim_as)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            fleet.people.forEach { person ->
                DropdownMenuItem(
                    text = { Text(person.name) },
                    onClick = {
                        onSelect(person.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
