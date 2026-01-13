package com.uwu.area

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uwu.area.ui.theme.AreaTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    currentSettings: AccessibilitySettings,
    onSettingsChanged: (AccessibilitySettings) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var localSettings by remember { mutableStateOf(currentSettings) }

    val handleSettingsChanged: (AccessibilitySettings) -> Unit = { newSettings ->
        localSettings = newSettings
        onSettingsChanged(newSettings)
    }

    AreaTheme(accessibilitySettings = localSettings) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.accessibility_settings),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackPressed,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                AccessibilitySection(title = stringResource(R.string.accessibility_language)) {
                    LanguageSelectionRow(
                        currentLanguage = localSettings.language,
                        onLanguageSelected = { language ->
                            handleSettingsChanged(localSettings.copy(language = language))
                        }
                    )
                }

                AccessibilitySection(title = stringResource(R.string.accessibility_theme)) {
                    ThemeSelectionRow(
                        currentTheme = localSettings.theme,
                        onThemeSelected = { theme ->
                            handleSettingsChanged(localSettings.copy(theme = theme))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = localSettings.highContrast,
                                onClick = {
                                    handleSettingsChanged(localSettings.copy(highContrast = !localSettings.highContrast))
                                },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.accessibility_high_contrast),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = localSettings.highContrast,
                            onCheckedChange = { checked ->
                                handleSettingsChanged(localSettings.copy(highContrast = checked))
                            }
                        )
                    }
                }

                AccessibilitySection(title = stringResource(R.string.accessibility_font_scale)) {
                    FontScaleSelectionRow(
                        currentFontScale = localSettings.fontScale,
                        onFontScaleSelected = { fontScale ->
                            handleSettingsChanged(localSettings.copy(fontScale = fontScale))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AccessibilitySection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            content()
        }
    }
}

@Composable
private fun LanguageSelectionRow(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Column {
        AppLanguage.values().forEach { language ->
            val isSelected = currentLanguage == language
            val languageName = when (language) {
                AppLanguage.ENGLISH -> "English"
                AppLanguage.FRENCH -> "Français"
                AppLanguage.SYSTEM -> stringResource(R.string.accessibility_theme_system)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onLanguageSelected(language) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = languageName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Column {
        AppTheme.values().forEach { theme ->
            val isSelected = currentTheme == theme
            val themeName = when (theme) {
                AppTheme.LIGHT -> stringResource(R.string.accessibility_theme_light)
                AppTheme.DARK -> stringResource(R.string.accessibility_theme_dark)
                AppTheme.SYSTEM -> stringResource(R.string.accessibility_theme_system)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onThemeSelected(theme) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = themeName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun FontScaleSelectionRow(
    currentFontScale: FontScale,
    onFontScaleSelected: (FontScale) -> Unit
) {
    Column {
        FontScale.values().forEach { fontScale ->
            val isSelected = currentFontScale == fontScale
            val fontScaleName = when (fontScale) {
                FontScale.SMALL -> stringResource(R.string.accessibility_font_scale_small)
                FontScale.MEDIUM -> stringResource(R.string.accessibility_font_scale_medium)
                FontScale.LARGE -> stringResource(R.string.accessibility_font_scale_large)
                FontScale.EXTRA_LARGE -> stringResource(R.string.accessibility_font_scale_extra_large)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onFontScaleSelected(fontScale) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = fontScaleName,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}