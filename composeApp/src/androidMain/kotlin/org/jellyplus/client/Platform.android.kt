package org.jellyplus.client

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

@Composable
actual fun getUiType(): UiType {
    val context = LocalContext.current
    val configuration = context.resources.configuration
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

    val isTv = uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION

    // We use smallestScreenWidthDp (sw600dp) to identify tablets.
    // This is orientation-independent, so a phone in landscape will NOT
    // accidentally trigger the Tablet/Desktop UI.
    val isTablet = configuration.smallestScreenWidthDp >= 600

    return if (isTv || isTablet) {
        UiType.Desktop
    } else {
        UiType.Mobile
    }
}

@Composable
actual fun provideSettings(): Settings {
    val context = LocalContext.current
    return remember {
        SharedPreferencesSettings(context.getSharedPreferences("jellyplus_settings", Context.MODE_PRIVATE))
    }
}

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}

actual fun getDeviceName(): String {
    return "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
}
