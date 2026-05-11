package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.ui.desktop.screens.DesktopSearchScreen
import org.jellyplex.client.ui.mobile.screens.MobileSearchScreen
import org.jellyplex.client.ui.viewmodels.SearchViewModel

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
