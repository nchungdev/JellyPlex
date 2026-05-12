package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.ui.desktop.screens.DesktopAuthHomeScreen
import org.jellyplus.client.ui.mobile.screens.MobileAuthHomeScreen

@Composable
fun AuthHomeScreen(
    baseUrl: String,
    onQuickConnect: () -> Unit,
    onManualLogin: () -> Unit,
    onChangeServer: () -> Unit,
) {
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopAuthHomeScreen(baseUrl, onQuickConnect, onManualLogin, onChangeServer)
    } else {
        MobileAuthHomeScreen(baseUrl, onQuickConnect, onManualLogin, onChangeServer)
    }
}
