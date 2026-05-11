package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.ui.desktop.screens.DesktopManualLoginScreen
import org.jellyplex.client.ui.mobile.screens.MobileManualLoginScreen
import org.jellyplex.client.ui.viewmodels.LoginState

@Composable
fun ManualLoginScreen(
    state: LoginState,
    currentUrl: String,
    onLogin: (String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopManualLoginScreen(state, currentUrl, onLogin, onBack)
    } else {
        MobileManualLoginScreen(state, currentUrl, onLogin, onBack)
    }
}
