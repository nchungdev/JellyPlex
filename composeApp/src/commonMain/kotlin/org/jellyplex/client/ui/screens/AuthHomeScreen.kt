package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.ui.desktop.screens.DesktopAuthHomeScreen
import org.jellyplex.client.ui.mobile.screens.MobileAuthHomeScreen

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
