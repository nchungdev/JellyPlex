package org.jellyplex.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.jellyplex.client.LocalUiType
import org.jellyplex.client.UiType
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.ui.mobile.screens.MobileSeriesDetailScreen
import org.jellyplex.client.ui.viewmodels.SeriesDetailViewModel
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
        org.jellyplex.client.ui.desktop.screens.DesktopSeriesDetailScreen(
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
