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
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onPlayEpisode: (MediaItem, MediaItem?, List<MediaItem>) -> Unit,
) {
    val uiType = LocalUiType.current
    val viewModel: SeriesDetailViewModel = koinViewModel()

    LaunchedEffect(item.id) {
        viewModel.loadSeriesDetails(item.id)
    }

    if (uiType == UiType.Desktop) {
        org.jellyplus.client.ui.desktop.screens.DesktopSeriesDetailScreen(
            item = item,
            viewModel = viewModel,
            onBack = onBack,
            onPlay = onPlay,
            onPlayEpisode = onPlayEpisode,
        )
    } else {
        MobileSeriesDetailScreen(
            item = item,
            viewModel = viewModel,
            onBack = onBack,
            onPlay = onPlay,
            onPlayEpisode = onPlayEpisode,
        )
    }
}
