package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.ui.desktop.screens.DesktopQuickConnectScreen
import org.jellyplus.client.ui.mobile.screens.MobileQuickConnectScreen
import org.jellyplus.client.ui.viewmodels.QuickConnectState

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
