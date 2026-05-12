package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.ui.desktop.screens.DesktopDownloadsScreen
import org.jellyplus.client.ui.mobile.screens.MobileDownloadsScreen
import org.jellyplus.client.ui.viewmodels.DownloadsViewModel

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
