package org.jellyplus.client

import androidx.compose.runtime.Composable
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

@Composable
actual fun getUiType(): UiType = UiType.Desktop

@Composable
actual fun provideSettings(): Settings {
    return PreferencesSettings(Preferences.userRoot().node("jellyplus_settings"))
}

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system back button on desktop
}

actual fun getDeviceName(): String {
    return System.getProperty("os.name") + " (" + System.getProperty("user.name") + ")"
}

actual fun isDebug(): Boolean = System.getProperty("jellyplus.debug") == "true"
