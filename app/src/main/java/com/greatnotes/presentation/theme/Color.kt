package com.greatnotes.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColorScheme(
    primary = Color(0xFF2962FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE4FF),
    onPrimaryContainer = Color(0xFF001257),
    secondary = Color(0xFF5A5F72),
    background = Color(0xFFFAF8F5),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFAF8F5),
    onSurface = Color(0xFF1B1B1F),
    error = Color(0xFFBA1A1A),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFB5C4FF),
    onPrimary = Color(0xFF002584),
    primaryContainer = Color(0xFF1B3DB0),
    onPrimaryContainer = Color(0xFFDCE4FF),
    secondary = Color(0xFFC2C5DC),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE4E2E6),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE4E2E6),
    error = Color(0xFFFFB4AB),
)
