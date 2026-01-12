package com.uwu.area.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Blue2734bd,
    onPrimary = White,
    secondary = Blue2734bd,
    onSecondary = White,
    error = ErrorRed,
    onError = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = Blue2734bd,
    onPrimary = White,
    secondary = Blue2734bd,
    onSecondary = White,
    error = ErrorRed,
    onError = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black
)

@Composable
fun AreaTheme(
    darkTheme: Boolean = false, // Default to light theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}