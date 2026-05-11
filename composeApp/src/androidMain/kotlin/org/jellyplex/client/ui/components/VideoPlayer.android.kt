package org.jellyplex.client.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import org.jellyplex.client.UiType
import org.jellyplex.client.data.remote.IntroMarker

@OptIn(UnstableApi::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
actual fun VideoPlayerImpl(
    item: org.jellyplex.client.domain.models.MediaItem,
    parentItem: org.jellyplex.client.domain.models.MediaItem?,
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
    when (uiType) {
        UiType.Desktop -> {
            org.jellyplex.client.ui.components.player.tv.DesktopVideoPlayer(
                item = item,
                parentItem = parentItem,
                url = url,
                accessToken = accessToken,
                playSessionId = playSessionId,
                mimeType = mimeType,
                markers = markers,
                onBack = onBack,
                onSkipEnding = onSkipEnding,
                onNextEpisode = onNextEpisode,
                onPrevEpisode = onPrevEpisode,
                onSpeedChange = onSpeedChange,
                onPlaybackStart = onPlaybackStart,
                onPlaybackProgress = onPlaybackProgress,
                onPlaybackStopped = onPlaybackStopped,
                showSkipEnding = showSkipEnding,
                showNextPrev = showNextPrev,
                playbackSpeed = playbackSpeed,
            )
        }

        else -> {
            org.jellyplex.client.ui.components.player.mobile.MobileVideoPlayer(
                item = item,
                parentItem = parentItem,
                url = url,
                accessToken = accessToken,
                playSessionId = playSessionId,
                mimeType = mimeType,
                markers = markers,
                onBack = onBack,
                onSkipEnding = onSkipEnding,
                onNextEpisode = onNextEpisode,
                onPrevEpisode = onPrevEpisode,
                onSpeedChange = onSpeedChange,
                onPlaybackStart = onPlaybackStart,
                onPlaybackProgress = onPlaybackProgress,
                onPlaybackStopped = onPlaybackStopped,
                showSkipEnding = showSkipEnding,
                showNextPrev = showNextPrev,
                playbackSpeed = playbackSpeed,
            )
        }
    }
}
