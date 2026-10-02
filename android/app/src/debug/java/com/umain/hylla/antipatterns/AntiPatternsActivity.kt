package com.umain.hylla.antipatterns

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.umain.hylla.layout.PaneLayout
import com.umain.hylla.posture.rememberWindowPosture
import com.umain.hylla.ui.theme.HyllaTheme

/**
 * Debug builds only. `adb shell am start -n com.umain.hylla/.antipatterns.AntiPatternsActivity`
 *
 * Two counters, one in `remember` and one in `rememberSaveable`. Tap both, then change the display
 * density (`adb shell wm density 300`): the activity is recreated, as it is for a fold or a
 * rotation, and only the saveable count survives. Below them, the wrong orientation rule and the
 * right one, applied to this window.
 */
class AntiPatternsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HyllaTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        var forgotten by remember { mutableIntStateOf(0) }
                        var kept by rememberSaveable { mutableIntStateOf(0) }
                        Text("Density ${LocalDensity.current.density}", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { forgotten++ }) { Text("remember: $forgotten") }
                        Button(onClick = { kept++ }) { Text("rememberSaveable: $kept") }

                        val posture = rememberWindowPosture()
                        Text("Window ${posture.widthDp.toInt()} × ${posture.heightDp.toInt()} dp")
                        Text("By orientation (wrong): ${AntiPatterns.panesByOrientation(posture.widthDp, posture.heightDp)} pane(s)")
                        Text("By window size (right): ${PaneLayout.compute(posture).paneCount} pane(s)")
                    }
                }
            }
        }
    }
}
