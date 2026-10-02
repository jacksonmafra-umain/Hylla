package com.umain.hylla

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.umain.hylla.fleet.DeepLink
import com.umain.hylla.fleet.DeviceId
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umain.hylla.posture.rememberWindowPosture
import com.umain.hylla.ui.theme.HyllaTheme

class MainActivity : ComponentActivity() {
    /** The latest device link, consumed by the navigation once it has opened it. */
    private val link = mutableStateOf<DeviceId?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Only on a fresh start: after recreation the saved back stack already shows the link.
        if (savedInstanceState == null) link.value = intent.deviceLink()
        setContent {
            HyllaTheme {
                HyllaApp(link.value, onLinkOpened = { link.value = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.deviceLink()?.let { link.value = it }
    }

    private fun Intent.deviceLink(): DeviceId? = dataString?.let(DeepLink::parse)
}

@Composable
fun HyllaApp(link: DeviceId? = null, onLinkOpened: () -> Unit = {}) {
    val app = LocalContext.current.applicationContext as HyllaApplication
    val fleet by app.store.fleet.collectAsStateWithLifecycle()
    val posture = rememberWindowPosture()
    HyllaNavigation(fleet, posture, app.store, app.me, app.notifications, app.watchList, link, onLinkOpened)
}
