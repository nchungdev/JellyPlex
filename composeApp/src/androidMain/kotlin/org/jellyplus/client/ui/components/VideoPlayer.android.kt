package org.jellyplus.client.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.IntroMarker

@Composable
actual fun VideoPlayerImpl(
    item: MediaItem,
    parentItem: MediaItem?,
    url: String,
    accessToken: String,
    playSessionId: String?,
    mimeType: String?,
    markers: List<IntroMarker>,
    modifier: Modifier,
    onBack: () -> Unit,
    onSkipEnding: () -> Unit,
    onNextEpisode: () -> Unit,
    onPrevEpisode: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPlaybackStart: (String, String) -> Unit,
    onPlaybackProgress: (String, String, Long, Boolean) -> Unit,
    onPlaybackStopped: (String, String, Long) -> Unit,
    showSkipEnding: Boolean,
    showNextPrev: Boolean,
    playbackSpeed: Float,
    uiType: UiType,
) {
    if (uiType == UiType.Desktop) {
        org.jellyplus.client.ui.components.player.desktop.DesktopVideoPlayer(
            item = item,
            parentItem = parentItem,
            url = url,
            accessToken = accessToken,
            playSessionId = playSessionId,
            mimeType = mimeType,
            markers = markers,
            modifier = modifier,
            onBack = onBack,
            onPlaybackStart = onPlaybackStart,
            onPlaybackProgress = onPlaybackProgress,
            onPlaybackStopped = onPlaybackStopped,
            playbackSpeed = playbackSpeed,
            showNextPrev = showNextPrev,
            onNextEpisode = onNextEpisode,
            onPrevEpisode = onPrevEpisode
        )
    } else {
        org.jellyplus.client.ui.components.player.mobile.MobileVideoPlayer(
            item = item,
            parentItem = parentItem,
            url = url,
            accessToken = accessToken,
            playSessionId = playSessionId,
            mimeType = mimeType,
            markers = markers,
            modifier = modifier,
            onBack = onBack,
            onPlaybackStart = onPlaybackStart,
            onPlaybackProgress = onPlaybackProgress,
            onPlaybackStopped = onPlaybackStopped,
            playbackSpeed = playbackSpeed,
            showNextPrev = showNextPrev,
            onNextEpisode = onNextEpisode,
            onPrevEpisode = onPrevEpisode
        )
    }
}
