package org.jellyplex.client

import androidx.compose.runtime.Composable
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

@Composable
actual fun getUiType(): UiType = UiType.Desktop

@Composable
actual fun provideSettings(): Settings {
    return PreferencesSettings(Preferences.userRoot().node("jellyplex_settings"))
}

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system back button on desktop
}
