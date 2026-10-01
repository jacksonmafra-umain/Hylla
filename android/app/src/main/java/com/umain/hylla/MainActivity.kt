package com.umain.hylla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.umain.hylla.fleet.loadFleet
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
    HyllaNavigation(fleet, posture)
}
