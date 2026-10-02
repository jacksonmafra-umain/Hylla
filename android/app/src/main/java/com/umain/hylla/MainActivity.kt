package com.umain.hylla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val store = (LocalContext.current.applicationContext as HyllaApplication).store
    val fleet by store.fleet.collectAsStateWithLifecycle()
    val posture = rememberWindowPosture()
    HyllaNavigation(fleet, posture, store)
}
