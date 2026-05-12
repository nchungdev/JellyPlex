package org.jellyplex.client.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jellyplex.client.domain.models.IntroMarker

@Composable
expect fun VideoPlayerImpl(
    item: org.jellyplex.client.domain.models.MediaItem,
    parentItem: org.jellyplex.client.domain.models.MediaItem? = null,
    url: String,
    accessToken: String,
    playSessionId: String? = null,
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
    uiType: org.jellyplex.client.UiType,
)

@Composable
fun VideoPlayer(
    item: org.jellyplex.client.domain.models.MediaItem,
    parentItem: org.jellyplex.client.domain.models.MediaItem? = null,
    url: String,
    accessToken: String = "",
    playSessionId: String? = null,
    mimeType: String? = null,
    markers: List<IntroMarker> = emptyList(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSkipEnding: () -> Unit = {},
    onNextEpisode: () -> Unit = {},
    onPrevEpisode: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    onPlaybackStart: (String, String) -> Unit = { _, _ -> },
    onPlaybackProgress: (String, String, Long, Boolean) -> Unit = { _, _, _, _ -> },
    onPlaybackStopped: (String, String, Long) -> Unit = { _, _, _ -> },
    showSkipEnding: Boolean = false,
    showNextPrev: Boolean = false,
    playbackSpeed: Float = 1.0f,
    uiType: org.jellyplex.client.UiType = org.jellyplex.client.UiType.Mobile,
) {
    VideoPlayerImpl(
        item,
        parentItem,
        url,
        accessToken,
        playSessionId,
        mimeType,
        markers,
        modifier,
        onBack,
        onSkipEnding,
        onNextEpisode,
        onPrevEpisode,
        onSpeedChange,
        onPlaybackStart,
        onPlaybackProgress,
        onPlaybackStopped,
        showSkipEnding,
        showNextPrev,
        playbackSpeed,
        uiType,
    )
}
