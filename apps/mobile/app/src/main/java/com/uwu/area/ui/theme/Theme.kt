package com.uwu.area.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.uwu.area.AccessibilitySettings
import com.uwu.area.AppTheme

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

private val HighContrastDarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = White,
    onSecondary = Black,
    error = ErrorRed,
    onError = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = Black,
    onSurfaceVariant = White,
    outline = White,
    outlineVariant = White
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Black,
    onSecondary = White,
    error = ErrorRed,
    onError = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = White,
    onSurfaceVariant = Black,
    outline = Black,
    outlineVariant = Black
)

@Composable
fun AreaTheme(
    accessibilitySettings: AccessibilitySettings = AccessibilitySettings(),
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()

    val useDarkTheme = when (accessibilitySettings.theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = when {
        accessibilitySettings.highContrast && useDarkTheme -> HighContrastDarkColorScheme
        accessibilitySettings.highContrast && !useDarkTheme -> HighContrastLightColorScheme
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getScaledTypography(accessibilitySettings.fontScale),
        content = content
    )
}