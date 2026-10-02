package com.umain.hylla.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** A runtime permission's state, and the one action that moves it forward from there. */
class RuntimePermission(val state: PermissionState, val request: () -> Unit, val openSettings: () -> Unit)

/**
 * Tracks [permission] and offers the right next step. Re-reads it on every resume, because the
 * user can change it in Settings while the app is in the background.
 */
@Composable
fun rememberRuntimePermission(permission: String): RuntimePermission {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val preferences = remember { context.getSharedPreferences("hylla.permissions", Context.MODE_PRIVATE) }
    var revision by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { revision++ }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { revision++ }

    val state = remember(revision) {
        PermissionState.of(
            granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED,
            askedBefore = preferences.getBoolean(permission, false),
            shouldShowRationale = activity?.shouldShowRequestPermissionRationale(permission) == true,
        )
    }
    return RuntimePermission(
        state = state,
        request = {
            preferences.edit { putBoolean(permission, true) }
            launcher.launch(permission)
        },
        openSettings = { context.openAppSettings() },
    )
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
