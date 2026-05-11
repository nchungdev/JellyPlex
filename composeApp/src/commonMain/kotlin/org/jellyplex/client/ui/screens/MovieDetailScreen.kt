package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.ui.desktop.screens.DesktopMovieDetailScreen
import org.jellyplex.client.ui.mobile.screens.MobileMovieDetailScreen
import org.jellyplex.client.ui.viewmodels.MovieDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MovieDetailScreen(
    item: MediaItem,
    onBack: () -> Unit,
    onPlay: (MediaItem) -> Unit,
) {
    val uiType = LocalUiType.current
    val viewModel: MovieDetailViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(item.id) {
        viewModel.loadMovieDetails(item)
    }

    if (uiType == UiType.Desktop) {
        DesktopMovieDetailScreen(
            item = item,
            baseUrl = state.baseUrl,
            viewModel = viewModel,
            onBack = onBack,
            onPlay = onPlay,
        )
    } else {
        MobileMovieDetailScreen(
            item = item,
            viewModel = viewModel,
            onBack = onBack,
            onPlay = onPlay,
        )
    }
}
