package org.jellyplus.client

import androidx.compose.runtime.Composable
import com.russhwolf.settings.Settings

@Composable
actual fun getUiType(): UiType = UiType.Mobile // Web usually behaves like mobile/browser

@Composable
actual fun provideSettings(): Settings {
    return Settings()
}

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system back button in browser normally
}
