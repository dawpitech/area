package com.uwu.area

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import java.util.Locale

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

enum class FontScale {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE
}

enum class AppLanguage {
    ENGLISH,
    FRENCH,
    SYSTEM
}

data class AccessibilitySettings(
    val theme: AppTheme = AppTheme.LIGHT,
    val fontScale: FontScale = FontScale.SMALL,
    val highContrast: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM
)

class AccessibilitySettingsManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("accessibility_prefs", Context.MODE_PRIVATE)

    fun getSettings(): AccessibilitySettings {
        return AccessibilitySettings(
            theme = AppTheme.valueOf(prefs.getString("theme", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name),
            fontScale = FontScale.valueOf(prefs.getString("font_scale", FontScale.MEDIUM.name) ?: FontScale.MEDIUM.name),
            highContrast = prefs.getBoolean("high_contrast", false),
            language = AppLanguage.valueOf(prefs.getString("language", AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name)
        )
    }

    fun saveSettings(settings: AccessibilitySettings) {
        prefs.edit().apply {
            putString("theme", settings.theme.name)
            putString("font_scale", settings.fontScale.name)
            putBoolean("high_contrast", settings.highContrast)
            putString("language", settings.language.name)
        }.apply()
    }

    fun applyLanguageSetting(language: AppLanguage) {
        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale("en")
            AppLanguage.FRENCH -> Locale("fr")
            AppLanguage.SYSTEM -> Locale.getDefault()
        }

        val configuration = context.resources.configuration
        configuration.setLocale(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }
}

@Composable
fun rememberAccessibilitySettings(): Pair<AccessibilitySettings, (AccessibilitySettings) -> Unit> {
    val context = LocalContext.current
    val manager = remember { AccessibilitySettingsManager(context) }
    val settings = remember { mutableStateOf(manager.getSettings()) }

    return settings.value to { newSettings: AccessibilitySettings ->
        manager.saveSettings(newSettings)
        manager.applyLanguageSetting(newSettings.language)
        settings.value = newSettings // Update the state to trigger recomposition
    }
}

fun getScaledTouchTargetSize(baseSize: Dp = 44.dp, fontScale: FontScale): Dp {
    val scaleFactor = when (fontScale) {
        FontScale.SMALL -> 0.8f
        FontScale.MEDIUM -> 1.0f
        FontScale.LARGE -> 1.2f
        FontScale.EXTRA_LARGE -> 1.4f
    }
    return (baseSize.value * scaleFactor).dp
}

fun getFontScaleMultiplier(fontScale: FontScale): Float {
    return when (fontScale) {
        FontScale.SMALL -> 0.85f
        FontScale.MEDIUM -> 1.0f
        FontScale.LARGE -> 1.15f
        FontScale.EXTRA_LARGE -> 1.3f
    }
}