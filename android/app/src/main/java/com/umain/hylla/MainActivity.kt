package com.umain.hylla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umain.hylla.posture.rememberWindowPosture
import com.umain.hylla.ui.theme.HyllaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HyllaTheme {
                HyllaApp()
            }
        }
    }
}

@Composable
fun HyllaApp() {
    val app = LocalContext.current.applicationContext as HyllaApplication
    val fleet by app.store.fleet.collectAsStateWithLifecycle()
    val posture = rememberWindowPosture()
    HyllaNavigation(fleet, posture, app.store, app.me)
}
