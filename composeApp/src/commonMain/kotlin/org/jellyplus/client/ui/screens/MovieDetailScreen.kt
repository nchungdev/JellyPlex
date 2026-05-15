package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.desktop.screens.DesktopMovieDetailScreen
import org.jellyplus.client.ui.mobile.screens.MobileMovieDetailScreen
import org.jellyplus.client.ui.viewmodels.MovieDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MovieDetailScreen(
    item: MediaItem,
    recommendedItems: List<MediaItem> = emptyList(),
    onBack: () -> Unit,
    onPlay: (MediaItem) -> Unit,
    onRecommendedClick: (MediaItem) -> Unit = {},
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
            recommendedItems = recommendedItems,
            onBack = onBack,
            onPlay = onPlay,
            onRecommendedClick = onRecommendedClick,
        )
    }
}
