package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.domain.models.RemoteServerLogin
import org.jellyplus.client.ui.desktop.screens.DesktopServerSelectionScreen
import org.jellyplus.client.ui.mobile.screens.MobileServerSelectionScreen
import org.jellyplus.client.ui.viewmodels.DiscoveryState

@Composable
fun ServerSelectionScreen(
    state: DiscoveryState,
    recentServers: List<RemoteServerLogin>,
    onScan: () -> Unit,
    onCancelScan: () -> Unit,
    onServerSelected: (DiscoveredServer) -> Unit,
    onRecentServerSelected: (RemoteServerLogin) -> Unit,
    onManualInput: (String) -> Unit,
    onTryDemo: () -> Unit,
) {
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopServerSelectionScreen(state, recentServers, onScan, onCancelScan, onServerSelected, onRecentServerSelected, onManualInput, onTryDemo)
    } else {
        MobileServerSelectionScreen(state, recentServers, onScan, onCancelScan, onServerSelected, onRecentServerSelected, onManualInput, onTryDemo)
    }
}
