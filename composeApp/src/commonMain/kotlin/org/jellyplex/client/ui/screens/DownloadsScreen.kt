package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.ui.desktop.screens.DesktopDownloadsScreen
import org.jellyplex.client.ui.mobile.screens.MobileDownloadsScreen
import org.jellyplex.client.ui.viewmodels.DownloadsViewModel

@Composable
fun DownloadsScreen(viewModel: DownloadsViewModel) {
    val state by viewModel.state.collectAsState()
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopDownloadsScreen(state)
    } else {
        MobileDownloadsScreen(state)
    }
}
