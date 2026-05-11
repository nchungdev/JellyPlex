package org.jellyplex.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.jellyplex.client.data.remote.IntroMarker

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
    uiType: org.jellyplex.client.UiType,
) {
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text("Desktop Video Player Placeholder", color = Color.White, fontSize = 24.sp)
        // In a real app, we'd use VLCJ or similar for Desktop
    }
}
