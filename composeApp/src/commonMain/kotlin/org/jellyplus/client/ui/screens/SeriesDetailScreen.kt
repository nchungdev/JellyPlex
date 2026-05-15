package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.mobile.screens.MobileSeriesDetailScreen
import org.jellyplus.client.ui.viewmodels.SeriesDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SeriesDetailScreen(
    item: MediaItem,
    focusSeasonId: String? = null,
    recommendedItems: List<MediaItem> = emptyList(),
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onPlayEpisode: (MediaItem, MediaItem?, List<MediaItem>) -> Unit,
    onRecommendedClick: (MediaItem) -> Unit = {},
    isFavorite: (MediaItem) -> Boolean = { it.userData?.isFavorite == true },
    onToggleFavorite: (MediaItem) -> Unit = {},
    isWatchLater: (MediaItem) -> Boolean = { false },
    onToggleWatchLater: (MediaItem) -> Unit = {},
) {
    val uiType = LocalUiType.current
    val viewModel: SeriesDetailViewModel = koinViewModel()

    LaunchedEffect(item.id, focusSeasonId) {
        viewModel.loadSeriesDetails(item.id, focusSeasonId)
    }

    if (uiType == UiType.Desktop) {
        org.jellyplus.client.ui.desktop.screens.DesktopSeriesDetailScreen(
            item = item,
            viewModel = viewModel,
            onBack = onBack,
            onPlay = onPlay,
            onPlayEpisode = onPlayEpisode,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            isWatchLater = isWatchLater,
            onToggleWatchLater = onToggleWatchLater,
        )
    } else {
        MobileSeriesDetailScreen(
            item = item,
            viewModel = viewModel,
            recommendedItems = recommendedItems,
            onBack = onBack,
            onPlay = onPlay,
            onPlayEpisode = onPlayEpisode,
            onRecommendedClick = onRecommendedClick,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            isWatchLater = isWatchLater,
            onToggleWatchLater = onToggleWatchLater,
        )
    }
}
