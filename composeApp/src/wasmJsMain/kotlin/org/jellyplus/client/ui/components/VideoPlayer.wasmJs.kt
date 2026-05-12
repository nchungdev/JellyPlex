package org.jellyplus.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import org.jellyplus.client.domain.models.IntroMarker
import org.jellyplus.client.domain.models.PlaybackConfig

@Composable
actual fun VideoPlayerImpl(
    item: org.jellyplus.client.domain.models.MediaItem,
    parentItem: org.jellyplus.client.domain.models.MediaItem?,
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
    uiType: org.jellyplus.client.UiType,
    nextEpisodeConfig: PlaybackConfig?,
    autoSkipIntro: Boolean,
    customMarkers: List<Pair<Long, Long>>,
    onPreloadNextMeta: () -> Unit,
    onMarkCurrentAsPlayed: () -> Unit,
    onSaveCustomMarker: (Long, Long) -> Unit,
    onToggleAutoSkip: () -> Unit,
    onSeamlessNextEpisode: () -> Unit,
    autoNext: Boolean,
    onToggleAutoNext: () -> Unit,
    autoSkipOutro: Boolean,
    onToggleAutoSkipOutro: () -> Unit,
    autoSkipPreview: Boolean,
    onToggleAutoSkipPreview: () -> Unit,
) {
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.05f) } // Start at 5%
    var isSyncPlayActive by remember { mutableStateOf(true) }

    val showSkipIntro = progress > 0.02f && progress < 0.15f

    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(5000)
            isControlsVisible = false
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    isControlsVisible = !isControlsVisible
                },
    ) {
        // Mock Video Content
        Box(modifier = Modifier.fillMaxSize())

        PlayerControls(
            title = "Now Playing: Big Buck Bunny",
            isVisible = isControlsVisible,
            isPlaying = isPlaying,
            progress = progress,
            showSkipIntro = showSkipIntro,
            showSkipEnding = showSkipEnding,
            showNextPrev = showNextPrev,
            isSyncPlayActive = isSyncPlayActive,
            playbackSpeed = playbackSpeed,
            onPlayPause = { isPlaying = !isPlaying },
            onSeek = { progress = it },
            onSkipIntro = {
                // Skip past 15%
                progress = 0.16f
            },
            onSkipEnding = {
                progress = 0.95f
            },
            onNextEpisode = onNextEpisode,
            onPrevEpisode = onPrevEpisode,
            onSpeedChange = onSpeedChange,
            onBack = onBack,
        )
    }
}
