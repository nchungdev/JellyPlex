package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.domain.discovery.DiscoveredServer
import org.jellyplex.client.ui.desktop.screens.DesktopServerSelectionScreen
import org.jellyplex.client.ui.mobile.screens.MobileServerSelectionScreen
import org.jellyplex.client.ui.viewmodels.DiscoveryState

@Composable
fun ServerSelectionScreen(
    state: DiscoveryState,
    onScan: () -> Unit,
    onCancelScan: () -> Unit,
    onServerSelected: (DiscoveredServer) -> Unit,
    onManualInput: (String) -> Unit,
) {
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopServerSelectionScreen(state, onScan, onCancelScan, onServerSelected, onManualInput)
    } else {
        MobileServerSelectionScreen(state, onScan, onCancelScan, onServerSelected, onManualInput)
    }
}
