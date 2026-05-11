package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.ui.desktop.screens.DesktopQuickConnectScreen
import org.jellyplex.client.ui.mobile.screens.MobileQuickConnectScreen
import org.jellyplex.client.ui.viewmodels.QuickConnectState

@Composable
fun QuickConnectScreen(
    state: QuickConnectState,
    onBack: () -> Unit,
) {
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopQuickConnectScreen(state, onBack)
    } else {
        MobileQuickConnectScreen(state, onBack)
    }
}
