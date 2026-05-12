package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.desktop.screens.DesktopSearchScreen
import org.jellyplus.client.ui.mobile.screens.MobileSearchScreen
import org.jellyplus.client.ui.viewmodels.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMediaClick: (MediaItem) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val uiType = LocalUiType.current

    if (uiType == UiType.Desktop) {
        DesktopSearchScreen(
            state = state,
            onQueryChange = { viewModel.onQueryChange(it) },
            onMediaClick = onMediaClick,
        )
    } else {
        MobileSearchScreen(
            state = state,
            onQueryChange = { viewModel.onQueryChange(it) },
            onMediaClick = onMediaClick,
        )
    }
}
