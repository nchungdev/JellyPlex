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
    autoSkipIntro: Boolean,
    autoSkipOutro: Boolean,
    autoNext: Boolean,
    autoNextCountdown: Int,
    nextEpisodeConfig: PlaybackConfig?,
    metaPreloaded: Boolean,
    videoPreloaded: Boolean,
    buildSecondaryPlayer: (PlaybackConfig) -> ExoPlayer?,
    onPositionUpdate: (position: Long, duration: Long) -> Unit,
    onMarkerUpdate: (inRange: Boolean, endMs: Long, type: String?) -> Unit,
    onMetaPreloaded: () -> Unit,
    onSecondaryReady: (ExoPlayer) -> Unit,
    onSeamlessSwap: () -> Unit,
    onAutoSkipped: (type: String?) -> Unit,
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

    // Main tracking loop: position, auto-skip, dual-player preload, seamless swap
    LaunchedEffect(isPlaying, isUserSeeking) {
        if (isUserSeeking) return@LaunchedEffect
        var lastSkippedEnd = -1L
        while (true) {
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration.coerceAtLeast(0L)
            onPositionUpdate(pos, dur)
            val remaining = if (dur > 0) dur - pos else Long.MAX_VALUE

            val introRanges = markers.filter { it.type == null || it.type == "Intro" }.map { it.startTicks / 10_000L to it.endTicks / 10_000L }
            val outroRanges = markers.filter { it.type == "Credits" || it.type == "Outro" }.map { it.startTicks / 10_000L to it.endTicks / 10_000L }
            val activeIntro = introRanges.firstOrNull { (s, e) -> pos in s..e }
            val activeOutro = outroRanges.firstOrNull { (s, e) -> pos in s..e }
            val activeMarker = activeIntro ?: activeOutro
            val activeType = if (activeOutro != null) "Credits" else null
            onMarkerUpdate(activeMarker != null, activeMarker?.second ?: 0L, activeType)
            when {
                autoSkipIntro && activeIntro != null -> {
                    if (lastSkippedEnd != activeIntro.second) {
                        lastSkippedEnd = activeIntro.second
                        onAutoSkipped(null)
                    }
                    exoPlayer.seekTo(activeIntro.second)
                }
                autoSkipOutro && activeOutro != null -> {
                    if (lastSkippedEnd != activeOutro.second) {
                        lastSkippedEnd = activeOutro.second
                        onAutoSkipped("Credits")
                    }
                    exoPlayer.seekTo(activeOutro.second)
                }
            }

            if (!metaPreloaded && remaining in 1..300_000L) onMetaPreloaded()
            if (!videoPreloaded && nextEpisodeConfig != null && remaining in 1..30_000L) {
                val secondary = buildSecondaryPlayer(nextEpisodeConfig)
                if (secondary != null) onSecondaryReady(secondary)
            }
            if (dur > 0 && pos >= dur - 500L) { onSeamlessSwap(); break }
            delay(500)
        }
    }
}
