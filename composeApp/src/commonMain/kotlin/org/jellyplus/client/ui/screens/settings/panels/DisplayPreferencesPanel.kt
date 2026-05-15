package org.jellyplus.client.ui.screens.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jellyplus.client.ui.screens.settings.components.PreferenceGroupCard
import org.jellyplus.client.ui.screens.settings.components.PreferenceInfoRow
import org.jellyplus.client.ui.screens.settings.components.PreferenceSelectRow
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesState

@Composable
internal fun DisplayPreferencesPanel(
    state: PlaybackPreferencesState,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onPosterDensityChange: (String) -> Unit,
    onImageQualityChange: (String) -> Unit,
    onMetadataDisplayChange: (String) -> Unit,
) {
    val languageOptions = listOf("System", "English", "Tiếng Việt")
    val themeOptions = listOf("System", "Dark", "Light")
    val posterDensityOptions = listOf("Compact", "Comfortable", "Large")
    val imageQualityOptions = listOf("Auto", "High", "Balanced", "Data saver")
    val metadataOptions = listOf("Title only", "Title and progress", "Full metadata")

    Column(modifier = Modifier.fillMaxWidth()) {
        PreferenceGroupCard("Language") {
            PreferenceSelectRow("App language", state.appLanguage, languageOptions, onLanguageChange)
            PreferenceInfoRow("System language", "System follows the device language when the app supports it.")
        }
        PreferenceGroupCard("Theme") {
            PreferenceSelectRow("Appearance", state.appTheme, themeOptions, onThemeChange)
            PreferenceInfoRow("Theme behavior", "System follows the device dark/light mode.")
        }
        PreferenceGroupCard("Library display", showDivider = false) {
            PreferenceSelectRow("Poster density", state.posterDensity, posterDensityOptions, onPosterDensityChange)
            PreferenceSelectRow("Image quality", state.imageQuality, imageQualityOptions, onImageQualityChange)
            PreferenceSelectRow("Metadata", state.metadataDisplay, metadataOptions, onMetadataDisplayChange)
        }
    }
}
