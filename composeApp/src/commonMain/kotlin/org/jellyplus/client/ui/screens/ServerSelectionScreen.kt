package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.ui.desktop.screens.DesktopServerSelectionScreen
import org.jellyplus.client.ui.mobile.screens.MobileServerSelectionScreen
import org.jellyplus.client.ui.viewmodels.DiscoveryState

@Composable
fun ServerSelectionScreen(
    state: DiscoveryState,
    onScan: () -> Unit,
    onCancelScan: () -> Unit,
    onServerSelected: (DiscoveredServer) -> Unit,
    onManualInput: (String) -> Unit,
    onTryDemo: () -> Unit,
) {
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopServerSelectionScreen(state, onScan, onCancelScan, onServerSelected, onManualInput, onTryDemo)
    } else {
        MobileServerSelectionScreen(state, onScan, onCancelScan, onServerSelected, onManualInput, onTryDemo)
    }
}
