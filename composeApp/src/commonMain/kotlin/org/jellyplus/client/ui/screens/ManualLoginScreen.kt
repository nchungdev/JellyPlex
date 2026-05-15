package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.ui.desktop.screens.DesktopManualLoginScreen
import org.jellyplus.client.ui.mobile.screens.MobileManualLoginScreen
import org.jellyplus.client.ui.viewmodels.LoginState

@Composable
fun ManualLoginScreen(
    state: LoginState,
    currentUrl: String,
    suggestedUsername: String,
    onLogin: (String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopManualLoginScreen(state, currentUrl, suggestedUsername, onLogin, onBack)
    } else {
        MobileManualLoginScreen(state, currentUrl, suggestedUsername, onLogin, onBack)
    }
}
