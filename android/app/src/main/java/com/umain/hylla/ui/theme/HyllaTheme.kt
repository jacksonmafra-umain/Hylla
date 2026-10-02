package com.umain.hylla.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand colours from the app icon (docs/assets/icon). Not dynamic colour: the codelab compares
// Android and iOS side by side, and both should look like the same app.
private val Teal = Color(0xFF12343B)
private val TealLight = Color(0xFF2E6B76)
private val Amber = Color(0xFFF2A541)
private val Cream = Color(0xFFF3EBDC)

private val Light = lightColorScheme(
    primary = TealLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBE7EC),
    onPrimaryContainer = Teal,
    secondary = Color(0xFF8A5A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB0),
    onSecondaryContainer = Color(0xFF2C1700),
    background = Color(0xFFFBFAF7),
    surface = Color(0xFFFBFAF7),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8FD0DB),
    onPrimary = Teal,
    primaryContainer = Color(0xFF1F4E57),
    onPrimaryContainer = Color(0xFFCBE7EC),
    secondary = Amber,
    onSecondary = Color(0xFF482A00),
    secondaryContainer = Color(0xFF66420A),
    onSecondaryContainer = Color(0xFFFFDDB0),
    background = Color(0xFF0E1A1D),
    surface = Color(0xFF0E1A1D),
    onSurface = Cream,
)

@Composable
fun HyllaTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
