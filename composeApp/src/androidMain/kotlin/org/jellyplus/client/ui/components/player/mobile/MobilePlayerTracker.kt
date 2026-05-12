package org.jellyplus.client.ui.components.player.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import org.jellyplus.client.domain.models.IntroMarker
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.domain.models.PlaybackConfig

/** Encapsulates all position-tracking LaunchedEffects, keeping MobileVideoPlayer lean. */
@Composable
internal fun MobilePlayerTracker(
    exoPlayer: ExoPlayer,
    item: org.jellyplus.client.domain.models.MediaItem,
    playSessionId: String?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isUserSeeking: Boolean,
    duration: Long,
    markers: List<IntroMarker>,
    customMarkers: List<Pair<Long, Long>>,
    autoSkipIntro: Boolean,
    autoSkipOutro: Boolean,
    autoSkipPreview: Boolean,
    autoNext: Boolean,
    autoNextCountdown: Int,
    markerState: MarkerState,
    markerStartMs: Long,
    nextEpisodeConfig: PlaybackConfig?,
    metaPreloaded: Boolean,
    videoPreloaded: Boolean,
    buildSecondaryPlayer: (PlaybackConfig) -> ExoPlayer?,
    onPositionUpdate: (position: Long, duration: Long) -> Unit,
    onMarkerUpdate: (inRange: Boolean, endMs: Long) -> Unit,
    onMetaPreloaded: () -> Unit,
    onSecondaryReady: (ExoPlayer) -> Unit,
    onSeamlessSwap: () -> Unit,
    onMarkEnd: (startMs: Long, endMs: Long) -> Unit,
    onMarkIdle: () -> Unit,
    onAutoNextTick: (countdown: Int) -> Unit,
    onAutoNextFire: () -> Unit,
    onPlaybackStart: (String, String) -> Unit,
    onPlaybackProgress: (String, String, Long, Boolean) -> Unit,
) {
    // Playback reporting
    LaunchedEffect(playSessionId, item.id) {
        val sessionId = playSessionId ?: return@LaunchedEffect
        onPlaybackStart(item.id, sessionId)
        while (true) { delay(10000); onPlaybackProgress(item.id, sessionId, exoPlayer.currentPosition * 10_000L, !isPlaying) }
    }

    // Auto-next countdown
    LaunchedEffect(autoNext, item.id) {
        if (!autoNext || item.type != MediaType.EPISODE || duration <= 0L) return@LaunchedEffect
        while (true) {
            val remaining = duration - exoPlayer.currentPosition
            if (remaining in 1..10_000L && autoNextCountdown == 0) onAutoNextTick((remaining / 1000).toInt().coerceAtLeast(1))
            if (autoNextCountdown > 0) {
                delay(1000)
                val next = (autoNextCountdown - 1).coerceAtLeast(0)
                onAutoNextTick(next)
                if (next == 0) { onAutoNextFire(); break }
            } else delay(500)
        }
    }

    // Main tracking loop: position, auto-skip, dual-player preload, seamless swap, custom marker end detection
    LaunchedEffect(isPlaying, isUserSeeking) {
        if (isUserSeeking) return@LaunchedEffect
        while (true) {
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration.coerceAtLeast(0L)
            onPositionUpdate(pos, dur)
            val remaining = if (dur > 0) dur - pos else Long.MAX_VALUE

            val introRanges = markers.filter { it.type == null || it.type == "Intro" }.map { it.startTicks / 10_000L to it.endTicks / 10_000L }
            val outroRanges = markers.filter { it.type == "Credits" || it.type == "Outro" }.map { it.startTicks / 10_000L to it.endTicks / 10_000L }
            val allRanges = introRanges + outroRanges + customMarkers
            val activeMarker = allRanges.firstOrNull { (s, e) -> pos in s..e }
            onMarkerUpdate(activeMarker != null, activeMarker?.second ?: 0L)

            val activeIntro = introRanges.firstOrNull { (s, e) -> pos in s..e }
            val activeOutro = outroRanges.firstOrNull { (s, e) -> pos in s..e }
            val activePreview = customMarkers.firstOrNull { (s, e) -> pos in s..e }
            when {
                autoSkipIntro && activeIntro != null -> exoPlayer.seekTo(activeIntro.second)
                autoSkipOutro && activeOutro != null -> exoPlayer.seekTo(activeOutro.second)
                autoSkipPreview && activePreview != null -> exoPlayer.seekTo(activePreview.second)
            }

            if (!metaPreloaded && remaining in 1..300_000L) onMetaPreloaded()
            if (!videoPreloaded && nextEpisodeConfig != null && remaining in 1..30_000L) {
                val secondary = buildSecondaryPlayer(nextEpisodeConfig)
                if (secondary != null) onSecondaryReady(secondary)
            }
            if (dur > 0 && pos >= dur - 500L) { onSeamlessSwap(); break }

            if (markerState == MarkerState.MARKING && isPlaying && !isBuffering) {
                onMarkEnd(markerStartMs, pos)
            } else if (markerState == MarkerState.IDLE) onMarkIdle()

            delay(500)
        }
    }
}
