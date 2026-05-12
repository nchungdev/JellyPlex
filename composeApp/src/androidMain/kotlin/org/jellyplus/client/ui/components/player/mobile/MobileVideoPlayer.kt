@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.jellyplus.client.ui.components.player.mobile

import android.annotation.SuppressLint
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import org.jellyplus.client.domain.models.IntroMarker
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.media.CustomHlsPlaylistParserFactory
import org.jellyplus.client.ui.common.components.player.PlayerAudioDialog
import org.jellyplus.client.ui.common.components.player.PlayerCaptionDialog
import org.jellyplus.client.ui.common.components.player.PlayerSettingsPopup
import kotlin.math.roundToInt

private fun PlayerView.applyTextureViewSurface() {
    try {
        val method = javaClass.getMethod("setSurfaceType", Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(this, 2)
    } catch (_: Exception) {}
}

@SuppressLint("UnsafeOptInUsageError")
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MobileVideoPlayer(
    item: org.jellyplus.client.domain.models.MediaItem,
    parentItem: org.jellyplus.client.domain.models.MediaItem? = null,
    url: String,
    accessToken: String,
    playSessionId: String? = null,
    mimeType: String?,
    markers: List<IntroMarker>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onPlaybackStart: (String, String) -> Unit,
    onPlaybackProgress: (String, String, Long, Boolean) -> Unit,
    onPlaybackStopped: (String, String, Long) -> Unit,
    playbackSpeed: Float,
    showNextPrev: Boolean = false,
    onNextEpisode: () -> Unit = {},
    onPrevEpisode: () -> Unit = {},
    nextEpisodeConfig: PlaybackConfig? = null,
    autoSkipIntro: Boolean = false,
    customMarkers: List<Pair<Long, Long>> = emptyList(),
    onPreloadNextMeta: () -> Unit = {},
    onMarkCurrentAsPlayed: () -> Unit = {},
    onSaveCustomMarker: (Long, Long) -> Unit = { _, _ -> },
    onToggleAutoSkip: () -> Unit = {},
    onSeamlessNextEpisode: () -> Unit = {},
    autoNext: Boolean = false,
    onToggleAutoNext: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    autoSkipOutro: Boolean = false,
    onToggleAutoSkipOutro: () -> Unit = {},
    autoSkipPreview: Boolean = false,
    onToggleAutoSkipPreview: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager }

    // Playback state
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isLongPressing by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var currentSpeed by remember { mutableStateOf(playbackSpeed) }

    // Gesture state
    var brightness by remember {
        val current = activity?.window?.attributes?.screenBrightness ?: 0.5f
        mutableStateOf(if (current < 0) 0.5f else current)
    }
    var volume by remember {
        mutableStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        )
    }
    var showGestureIndicator by remember { mutableStateOf(false) }
    var gestureType by remember { mutableStateOf("") }

    // Seek feedback
    var seekFeedback by remember { mutableStateOf("") }
    var showSeekFeedback by remember { mutableStateOf(false) }
    var seekFeedbackIsRight by remember { mutableStateOf(true) }

    // Marker state
    var isInMarkerRange by remember { mutableStateOf(false) }
    var currentMarkerEndMs by remember { mutableStateOf(0L) }
    var markerState by remember { mutableStateOf(MarkerState.IDLE) }
    var markerStartMs by remember { mutableStateOf(0L) }
    var stablePlaybackMs by remember { mutableStateOf(0L) }

    // Dialog state
    var showCaptionDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSettingsPopup by remember { mutableStateOf(false) }

    // Track selection
    var availableTextTracks by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var selectedTextTrackIndex by remember { mutableStateOf(-1) }
    var availableAudioTracks by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var selectedAudioTrackIndex by remember { mutableStateOf(0) }

    var autoNextCountdown by remember { mutableStateOf(0) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var showBufferingIndicator by remember { mutableStateOf(false) }

    val trackSelector = remember { DefaultTrackSelector(context) }
    val httpDataSourceFactory = remember {
        DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0")
            .setDefaultRequestProperties(mapOf("X-Emby-Authorization" to "MediaBrowser Client=\"JellyPlus\", Token=\"$accessToken\""))
    }
    val hlsMediaSourceFactory = remember {
        HlsMediaSource.Factory(httpDataSourceFactory).setPlaylistParserFactory(CustomHlsPlaylistParserFactory())
    }

    fun buildExoPlayer(streamUrl: String, streamMimeType: String?, startPlaying: Boolean): ExoPlayer {
        val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory)
        return ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).setTrackSelector(trackSelector).build().apply {
            val resolvedMime = when {
                streamMimeType?.lowercase()?.contains("hls") == true ||
                streamMimeType?.lowercase()?.contains("m3u8") == true ||
                streamMimeType?.lowercase()?.contains("m3u") == true ||
                streamUrl.contains(".m3u8") || streamUrl.contains(".m3u") || streamUrl.contains("hls") -> MimeTypes.APPLICATION_M3U8
                else -> streamMimeType
            }
            val mi = MediaItem.Builder().setUri(streamUrl).setMimeType(resolvedMime).build()
            if (resolvedMime == MimeTypes.APPLICATION_M3U8) setMediaSource(hlsMediaSourceFactory.createMediaSource(mi))
            else setMediaItem(mi)
            setPlaybackSpeed(playbackSpeed)
            prepare()
            playWhenReady = startPlaying
        }
    }

    // Dual player
    var metaPreloaded by remember { mutableStateOf(false) }
    var videoPreloaded by remember { mutableStateOf(false) }
    var isPrimaryActive by remember { mutableStateOf(true) }
    var secondaryExoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    val primaryExoPlayer = remember(url, mimeType, accessToken) {
        buildExoPlayer(url, mimeType, startPlaying = true).apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) { if (isPrimaryActive) isPlaying = playing }
                override fun onPlaybackStateChanged(state: Int) {
                    if (isPrimaryActive) {
                        duration = this@apply.duration.coerceAtLeast(0L)
                        isBuffering = state == Player.STATE_BUFFERING
                        if (state == Player.STATE_READY) playbackError = null
                    }
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    if (isPrimaryActive) {
                        playbackError = error.localizedMessage ?: "Playback Error"
                        isPlaying = false
                        // Circuit breaker: stop everything and reset states
                        this@apply.stop()
                        secondaryExoPlayer?.stop()
                        secondaryExoPlayer?.release()
                        secondaryExoPlayer = null
                        videoPreloaded = false
                        metaPreloaded = false
                        autoNextCountdown = 0
                    }
                }
                override fun onTracksChanged(tracks: Tracks) {
                    if (isPrimaryActive) {
                        val textTracks = mutableListOf<Pair<Int, String>>(); var tIdx = 0
                        val audioTracks = mutableListOf<Pair<Int, String>>(); var aIdx = 0
                        for (group in tracks.groups) {
                            when (group.type) {
                                C.TRACK_TYPE_TEXT -> for (i in 0 until group.length) {
                                    val f = group.getTrackFormat(i)
                                    textTracks += tIdx++ to (f.label?.takeIf { it.isNotBlank() } ?: f.language?.takeIf { it.isNotBlank() } ?: "Sub $tIdx")
                                }
                                C.TRACK_TYPE_AUDIO -> for (i in 0 until group.length) {
                                    val f = group.getTrackFormat(i)
                                    audioTracks += aIdx++ to (f.label?.takeIf { it.isNotBlank() } ?: f.language?.takeIf { it.isNotBlank() } ?: "Audio $aIdx")
                                }
                            }
                        }
                        availableTextTracks = textTracks; availableAudioTracks = audioTracks
                    }
                }
            })
        }
    }

    val currentPlayer = if (isPrimaryActive) primaryExoPlayer else (secondaryExoPlayer ?: primaryExoPlayer)

    LaunchedEffect(item.id) {
        isControlsVisible = false
        secondaryExoPlayer?.release()
        secondaryExoPlayer = null
        metaPreloaded = false
        videoPreloaded = false
        autoNextCountdown = 0
        isPrimaryActive = true
        isPlaying = true
        isBuffering = true
        isLongPressing = false
        showGestureIndicator = false
        gestureType = ""
        currentPosition = 0L
        duration = 0L
    }

    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(5000)
            isControlsVisible = false
        }
    }
    LaunchedEffect(isBuffering) {
        if (isBuffering) {
            delay(1000L)
            showBufferingIndicator = true
        } else {
            showBufferingIndicator = false
        }
    }
    LaunchedEffect(showGestureIndicator) { if (showGestureIndicator) { delay(2000); showGestureIndicator = false } }
    LaunchedEffect(showSeekFeedback) { if (showSeekFeedback) { delay(800); showSeekFeedback = false } }
    LaunchedEffect(playbackSpeed) { currentSpeed = playbackSpeed; currentPlayer.setPlaybackSpeed(playbackSpeed) }

    MobilePlayerTracker(
        exoPlayer = currentPlayer,
        item = item,
        playSessionId = playSessionId,
        isPlaying = isPlaying,
        isBuffering = isBuffering,
        isUserSeeking = isUserSeeking,
        duration = duration,
        markers = markers,
        customMarkers = customMarkers,
        autoSkipIntro = autoSkipIntro,
        autoSkipOutro = autoSkipOutro,
        autoSkipPreview = autoSkipPreview,
        autoNext = autoNext,
        autoNextCountdown = autoNextCountdown,
        markerState = markerState,
        markerStartMs = markerStartMs,
        nextEpisodeConfig = nextEpisodeConfig,
        metaPreloaded = metaPreloaded,
        videoPreloaded = videoPreloaded,
        buildSecondaryPlayer = { config ->
            try { buildExoPlayer(config.url, config.mimeType, startPlaying = false) }
            catch (_: Exception) { null }
        },
        onPositionUpdate = { pos, dur ->
            currentPosition = pos
            duration = dur
        },
        onMarkerUpdate = { inRange, endMs ->
            isInMarkerRange = inRange
            currentMarkerEndMs = endMs
        },
        onMetaPreloaded = { metaPreloaded = true; onPreloadNextMeta() },
        onSecondaryReady = { secondary ->
            secondary.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) { if (!isPrimaryActive) isPlaying = playing }
                override fun onPlaybackStateChanged(state: Int) {
                    if (!isPrimaryActive) {
                        duration = secondary.duration.coerceAtLeast(0L)
                        isBuffering = state == Player.STATE_BUFFERING
                    }
                }
                override fun onTracksChanged(tracks: Tracks) {
                    if (!isPrimaryActive) {
                        val textTracks = mutableListOf<Pair<Int, String>>(); var tIdx = 0
                        val audioTracks = mutableListOf<Pair<Int, String>>(); var aIdx = 0
                        for (group in tracks.groups) {
                            when (group.type) {
                                C.TRACK_TYPE_TEXT -> for (i in 0 until group.length) {
                                    val f = group.getTrackFormat(i)
                                    textTracks += tIdx++ to (f.label?.takeIf { it.isNotBlank() } ?: f.language?.takeIf { it.isNotBlank() } ?: "Sub $tIdx")
                                }
                                C.TRACK_TYPE_AUDIO -> for (i in 0 until group.length) {
                                    val f = group.getTrackFormat(i)
                                    audioTracks += aIdx++ to (f.label?.takeIf { it.isNotBlank() } ?: f.language?.takeIf { it.isNotBlank() } ?: "Audio $aIdx")
                                }
                            }
                        }
                        availableTextTracks = textTracks; availableAudioTracks = audioTracks
                    }
                }
            })
            secondaryExoPlayer = secondary
            videoPreloaded = true
        },
        onSeamlessSwap = {
            if (secondaryExoPlayer != null) {
                secondaryExoPlayer!!.play()
                isPrimaryActive = false
                onMarkCurrentAsPlayed()
                primaryExoPlayer.stop()
                primaryExoPlayer.release()
                metaPreloaded = false
                videoPreloaded = false
                onSeamlessNextEpisode()
            }
        },
        onMarkEnd = { start, pos ->
            if (stablePlaybackMs == 0L) stablePlaybackMs = pos
            if (pos - stablePlaybackMs >= 2000L) {
                android.util.Log.d("MarkPreview", "END: itemId=${item.id} start=${start}ms end=${pos}ms")
                onSaveCustomMarker(start, pos)
                markerState = MarkerState.IDLE
                stablePlaybackMs = 0L
            }
        },
        onMarkIdle = { stablePlaybackMs = 0L },
        onAutoNextTick = { autoNextCountdown = it },
        onAutoNextFire = { onNextEpisode() },
        onPlaybackStart = onPlaybackStart,
        onPlaybackProgress = onPlaybackProgress
    )

    DisposableEffect(Unit) {
        onDispose {
            val sessionId = playSessionId
            if (sessionId != null) onPlaybackStopped(item.id, sessionId, currentPlayer.currentPosition * 10_000L)
        }
    }

    DisposableEffect(primaryExoPlayer) {
        onDispose { primaryExoPlayer.release() }
    }

    DisposableEffect(secondaryExoPlayer) {
        onDispose { secondaryExoPlayer?.release() }
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black)
    ) {
        secondaryExoPlayer?.let { secondary ->
            AndroidView(
                factory = { ctx -> PlayerView(ctx).apply { player = secondary; useController = false; applyTextureViewSurface() } },
                modifier = Modifier.fillMaxSize().alpha(if (isPrimaryActive) 0f else 1f),
            )
        }
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { player = primaryExoPlayer; useController = false; applyTextureViewSurface() } },
            modifier = Modifier.fillMaxSize().alpha(if (isPrimaryActive) 1f else 0f),
        )

        // Gesture Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isControlsVisible) {
                    if (!isControlsVisible) {
                        detectVerticalDragGestures(
                            onDragStart = { /* Determined in onVerticalDrag */ },
                            onDragEnd = { /* Auto-hide by LaunchedEffect */ },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val isLeft = change.position.x < size.width / 2
                                if (isLeft) {
                                    gestureType = "brightness"
                                    val delta = (dragAmount / size.height) * -1f
                                    brightness = (brightness + delta).coerceIn(0f, 1f)
                                    activity?.let { a ->
                                        val params = a.window.attributes
                                        params.screenBrightness = brightness
                                        a.window.attributes = params
                                    }
                                    showGestureIndicator = true
                                } else {
                                    gestureType = "volume"
                                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val delta = (dragAmount / size.height) * -2f
                                    volume = (volume + delta).coerceIn(0f, 1f)
                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        (volume * maxVol).roundToInt(),
                                        AudioManager.FLAG_SHOW_UI
                                    )
                                    showGestureIndicator = false
                                }
                            }
                        )
                    }
                }
                .pointerInput(isControlsVisible, playbackSpeed) {
                    if (!isControlsVisible) awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val startTime = System.currentTimeMillis()
                        val up = withTimeoutOrNull(2000L) { waitForUpOrCancellation() }
                        val elapsed = System.currentTimeMillis() - startTime
                        // up == null có thể do timeout (long press thực sự) hoặc event bị consumed bởi handler khác.
                        // Chỉ kích hoạt speedup khi thực sự giữ đủ 2s.
                        if (up == null && elapsed >= 2000L) {
                            isLongPressing = true
                            currentPlayer.setPlaybackSpeed(2f)
                            try {
                                waitForUpOrCancellation()
                            } finally {
                                currentPlayer.setPlaybackSpeed(playbackSpeed)
                                isLongPressing = false
                            }
                        }
                    }
                }
                .pointerInput(isControlsVisible) {
                    detectTapGestures(
                        onTap = { if (!isLongPressing) isControlsVisible = !isControlsVisible },
                        onDoubleTap = { offset ->
                            if (!isLongPressing && !isControlsVisible) {
                                val isRight = offset.x >= size.width / 2
                                if (isRight) {
                                    currentPlayer.seekTo((currentPlayer.currentPosition + 10000).coerceAtMost(duration))
                                    seekFeedback = "+10"
                                } else {
                                    currentPlayer.seekTo((currentPlayer.currentPosition - 5000).coerceAtLeast(0))
                                    seekFeedback = "-5"
                                }
                                seekFeedbackIsRight = isRight
                                showSeekFeedback = true
                                stablePlaybackMs = 0L
                            }
                        }
                    )
                }
        )

        // Buffering spinner — always visible after 1s debounce, independent of controls overlay
        if (showBufferingIndicator && !isControlsVisible) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(48.dp).align(Alignment.Center),
                color = Color.White,
                strokeWidth = 3.dp,
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = isControlsVisible,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
                MobilePlayerTopBar(item = item, parentItem = parentItem, onBack = onBack, onMoreClick = { showSettingsPopup = true })
                MobilePlayerCenterControls(
                    isPlaying = isPlaying, isBuffering = isBuffering, showNextPrev = showNextPrev,
                    onPlayPause = { if (isPlaying) currentPlayer.pause() else currentPlayer.play() },
                    onRewind = { currentPlayer.seekTo((currentPlayer.currentPosition - 10000).coerceAtLeast(0)) },
                    onForward = { currentPlayer.seekTo((currentPlayer.currentPosition + 10000).coerceAtMost(duration)) },
                    onPrevEpisode = onPrevEpisode, onNextEpisode = onNextEpisode,
                    modifier = Modifier.align(Alignment.Center),
                )
                MobilePlayerBottomControls(
                    item = item, currentPosition = currentPosition, duration = duration,
                    markerState = markerState, markerStartMs = markerStartMs,
                    selectedTextTrackIndex = selectedTextTrackIndex,
                    onSeek = { currentPlayer.seekTo(it); stablePlaybackMs = 0L },
                    onSeekStarted = { isUserSeeking = true },
                    onSeekFinished = { pos ->
                        currentPlayer.seekTo(pos)
                        isUserSeeking = false
                        stablePlaybackMs = 0L
                    },
                    onShowCaptionDialog = { showCaptionDialog = true },
                    onShowAudioDialog = { showAudioDialog = true },
                    onMarkToggle = {
                        if (markerState == MarkerState.IDLE) {
                            markerStartMs = currentPosition; markerState = MarkerState.MARKING; stablePlaybackMs = 0L
                            android.util.Log.d("MarkPreview", "START: itemId=${item.id} pos=${currentPosition}ms")
                        } else {
                            android.util.Log.d("MarkPreview", "CANCELLED by user at pos=${currentPosition}ms")
                            markerState = MarkerState.IDLE; stablePlaybackMs = 0L
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                )
            }
        }

        MobilePlayerOverlays(
            item = item, isLongPressing = isLongPressing,
            showSeekFeedback = showSeekFeedback, seekFeedback = seekFeedback, seekFeedbackIsRight = seekFeedbackIsRight,
            showGestureIndicator = showGestureIndicator, gestureType = gestureType,
            volume = volume, brightness = brightness,
            isInMarkerRange = isInMarkerRange, currentMarkerEndMs = currentMarkerEndMs,
            markerState = markerState, autoNextCountdown = autoNextCountdown,
            isControlsVisible = isControlsVisible,
            onSkipMarker = { currentPlayer.seekTo(currentMarkerEndMs) },
            onCancelAutoNext = { autoNextCountdown = 0 },
            onCancelMarking = {
                android.util.Log.d("MarkPreview", "CANCELLED by user at pos=${currentPosition}ms")
                markerState = MarkerState.IDLE; stablePlaybackMs = 0L
            },
        )

        playbackError?.let { error ->
            Column(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f))
                    .pointerInput(Unit) { detectTapGestures { } },
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Playback failed", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                Text(text = error, color = Color.White.copy(alpha = 0.7f), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        playbackError = null
                        currentPlayer.prepare()
                        currentPlayer.play()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Try Again")
                }
                androidx.compose.material3.TextButton(onClick = onBack) {
                    Text("Go Back", color = Color.White)
                }
            }
        }
    }

    if (showCaptionDialog) PlayerCaptionDialog(
        availableTextTracks = availableTextTracks, selectedTextTrackIndex = selectedTextTrackIndex,
        onSelectOff = {
            selectedTextTrackIndex = -1
            trackSelector.setParameters(trackSelector.buildUponParameters().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true).build())
            showCaptionDialog = false
        },
        onSelectTrack = { idx ->
            selectedTextTrackIndex = idx
            trackSelector.setParameters(trackSelector.buildUponParameters().setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false).build())
            showCaptionDialog = false
        },
        onDismiss = { showCaptionDialog = false },
    )

    if (showAudioDialog) PlayerAudioDialog(
        availableAudioTracks = availableAudioTracks, selectedAudioTrackIndex = selectedAudioTrackIndex,
        onSelectTrack = { idx ->
            selectedAudioTrackIndex = idx
            trackSelector.setParameters(trackSelector.buildUponParameters().setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false).build())
            showAudioDialog = false
        },
        onDismiss = { showAudioDialog = false },
    )

    if (showSettingsPopup) PlayerSettingsPopup(
        autoSkipIntro = autoSkipIntro, autoSkipOutro = autoSkipOutro, autoSkipPreview = autoSkipPreview,
        autoNext = autoNext, isEpisode = item.type == MediaType.EPISODE,
        currentSpeed = currentSpeed,
        onToggleAutoSkip = onToggleAutoSkip, onToggleAutoSkipOutro = onToggleAutoSkipOutro,
        onToggleAutoSkipPreview = onToggleAutoSkipPreview, onToggleAutoNext = onToggleAutoNext,
        onSpeedChange = { speed -> currentSpeed = speed; currentPlayer.setPlaybackSpeed(speed); onSpeedChange(speed) },
        onDismiss = { showSettingsPopup = false },
    )
}
