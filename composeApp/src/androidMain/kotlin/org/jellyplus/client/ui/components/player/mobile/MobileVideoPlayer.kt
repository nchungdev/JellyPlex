@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package org.jellyplus.client.ui.components.player.mobile

import android.annotation.SuppressLint
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.jellyplus.client.domain.models.IntroMarker
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.media.CustomHlsPlaylistParserFactory

private enum class MarkerState { IDLE, MARKING }

private fun PlayerView.applyTextureViewSurface() {
    try {
        val method = javaClass.getMethod("setSurfaceType", Int::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(this, 2) // PlayerView.SURFACE_TYPE_TEXTURE_VIEW
    } catch (_: Exception) {}
}
@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalMaterial3Api::class)
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
    // Seamless playback
    nextEpisodeConfig: PlaybackConfig? = null,
    autoSkipIntro: Boolean = false,
    customMarkers: List<Pair<Long, Long>> = emptyList(),
    onPreloadNextMeta: () -> Unit = {},
    onMarkCurrentAsPlayed: () -> Unit = {},
    onSaveCustomMarker: (Long, Long) -> Unit = { _, _ -> },
    onToggleAutoSkip: () -> Unit = {},
    onSeamlessNextEpisode: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager }

    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isLongPressing by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    // Gesture states
    var brightness by remember { mutableStateOf(activity?.window?.attributes?.screenBrightness ?: 0.5f) }
    var volume by remember {
        mutableStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
        )
    }
    var showGestureIndicator by remember { mutableStateOf(false) }
    var gestureType by remember { mutableStateOf("") }

    // Seek feedback overlay (+10s / -5s)
    var seekFeedback by remember { mutableStateOf("") }
    var showSeekFeedback by remember { mutableStateOf(false) }
    var seekFeedbackIsRight by remember { mutableStateOf(true) }

    // Skip intro state
    var isInMarkerRange by remember { mutableStateOf(false) }
    var currentMarkerEndMs by remember { mutableStateOf(0L) }

    // Custom marker state machine
    var markerState by remember { mutableStateOf(MarkerState.IDLE) }
    var markerStartMs by remember { mutableStateOf(0L) }
    var stablePlaybackMs by remember { mutableStateOf(0L) }

    // Dual player flags
    var metaPreloaded by remember { mutableStateOf(false) }
    var videoPreloaded by remember { mutableStateOf(false) }
    var isPrimaryActive by remember { mutableStateOf(true) }

    val httpDataSourceFactory = remember {
        DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0")
            .setDefaultRequestProperties(
                mapOf("X-Emby-Authorization" to "MediaBrowser Client=\"JellyPlus\", Token=\"$accessToken\""),
            )
    }
    val hlsMediaSourceFactory = remember {
        HlsMediaSource.Factory(httpDataSourceFactory)
            .setPlaylistParserFactory(CustomHlsPlaylistParserFactory())
    }

    fun buildExoPlayer(streamUrl: String, streamMimeType: String?, startPlaying: Boolean): ExoPlayer {
        val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory)
        return ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build().apply {
            val resolvedMimeType = when {
                streamMimeType?.lowercase()?.contains("hls") == true -> MimeTypes.APPLICATION_M3U8
                streamMimeType?.lowercase()?.contains("m3u8") == true -> MimeTypes.APPLICATION_M3U8
                streamMimeType?.lowercase()?.contains("m3u") == true -> MimeTypes.APPLICATION_M3U8
                streamUrl.contains(".m3u8") || streamUrl.contains(".m3u") || streamUrl.contains("hls") -> MimeTypes.APPLICATION_M3U8
                else -> streamMimeType
            }
            val mediaItem = MediaItem.Builder().setUri(streamUrl).setMimeType(resolvedMimeType).build()
            if (resolvedMimeType == MimeTypes.APPLICATION_M3U8) {
                setMediaSource(hlsMediaSourceFactory.createMediaSource(mediaItem))
            } else {
                setMediaItem(mediaItem)
            }
            setPlaybackSpeed(playbackSpeed)
            prepare()
            playWhenReady = startPlaying
        }
    }

    val exoPlayer = remember {
        buildExoPlayer(url, mimeType, startPlaying = true).apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
                override fun onPlaybackStateChanged(state: Int) {
                    duration = this@apply.duration.coerceAtLeast(0L)
                    isBuffering = state == Player.STATE_BUFFERING
                }
            })
        }
    }

    var secondaryExoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Auto-hide controls
    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(5000)
            isControlsVisible = false
        }
    }

    LaunchedEffect(showGestureIndicator) {
        if (showGestureIndicator) { delay(2000); showGestureIndicator = false }
    }

    LaunchedEffect(showSeekFeedback) {
        if (showSeekFeedback) { delay(800); showSeekFeedback = false }
    }

    // Playback reporting
    LaunchedEffect(playSessionId, item.id) {
        val sessionId = playSessionId ?: return@LaunchedEffect
        onPlaybackStart(item.id, sessionId)
        while (true) {
            delay(10000)
            onPlaybackProgress(item.id, sessionId, exoPlayer.currentPosition * 10_000L, !isPlaying)
        }
    }

    // Main tracking loop (500ms)
    LaunchedEffect(isPlaying) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            val remaining = if (duration > 0) duration - currentPosition else Long.MAX_VALUE

            val allMarkerRanges = markers.map { it.startTicks / 10_000L to it.endTicks / 10_000L } + customMarkers
            val activeMarker = allMarkerRanges.firstOrNull { (start, end) -> currentPosition in start..end }
            isInMarkerRange = activeMarker != null
            currentMarkerEndMs = activeMarker?.second ?: 0L
            if (autoSkipIntro && activeMarker != null) exoPlayer.seekTo(currentMarkerEndMs)

            if (!metaPreloaded && remaining in 1..300_000L) {
                metaPreloaded = true; onPreloadNextMeta()
            }

            if (!videoPreloaded && nextEpisodeConfig != null && remaining in 1..30_000L) {
                videoPreloaded = true
                try {
                    val cfg = nextEpisodeConfig
                    secondaryExoPlayer = buildExoPlayer(cfg.url, cfg.mimeType, startPlaying = false)
                } catch (_: Exception) {
                    secondaryExoPlayer?.release(); secondaryExoPlayer = null
                }
            }

            if (duration > 0 && currentPosition >= duration - 500L && secondaryExoPlayer != null) {
                val secondary = secondaryExoPlayer!!
                secondary.play()
                isPrimaryActive = false
                onMarkCurrentAsPlayed()
                exoPlayer.stop(); exoPlayer.release()
                metaPreloaded = false; videoPreloaded = false; secondaryExoPlayer = null
                onSeamlessNextEpisode()
                break
            }

            // Custom marker end detection (2s stable playback after seek)
            if (markerState == MarkerState.MARKING && isPlaying && !isBuffering) {
                if (stablePlaybackMs == 0L) stablePlaybackMs = currentPosition
                if (currentPosition - stablePlaybackMs >= 2000L) {
                    onSaveCustomMarker(markerStartMs, currentPosition)
                    markerState = MarkerState.IDLE; stablePlaybackMs = 0L
                }
            } else if (markerState == MarkerState.IDLE) {
                stablePlaybackMs = 0L
            }

            delay(500)
        }
    }

    DisposableEffect(Unit) {
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            val sessionId = playSessionId
            if (sessionId != null) onPlaybackStopped(item.id, sessionId, exoPlayer.currentPosition * 10_000L)
            exoPlayer.release()
            secondaryExoPlayer?.release()
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (!isLongPressing) isControlsVisible = !isControlsVisible },
                    onDoubleTap = { offset ->
                        if (!isLongPressing) {
                            val isRight = offset.x >= size.width / 2
                            if (isRight) {
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                                seekFeedback = "+10"
                            } else {
                                exoPlayer.seekTo((exoPlayer.currentPosition - 5000).coerceAtLeast(0))
                                seekFeedback = "-5"
                            }
                            seekFeedbackIsRight = isRight
                            showSeekFeedback = true
                            stablePlaybackMs = 0L
                        }
                    },
                    onLongPress = {},
                )
            }
            .pointerInput(isControlsVisible, playbackSpeed) {
                if (!isControlsVisible) {
                    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        val up = withTimeoutOrNull(longPressTimeout) { waitForUpOrCancellation() }
                        if (up == null) {
                            isLongPressing = true
                            exoPlayer.setPlaybackSpeed(2f)
                            try { waitForUpOrCancellation() }
                            finally { exoPlayer.setPlaybackSpeed(playbackSpeed); isLongPressing = false }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { showGestureIndicator = true },
                    onDragEnd = {},
                    onVerticalDrag = { change, dragAmount ->
                        val isLeft = change.position.x < size.width / 2
                        if (isLeft) {
                            gestureType = "volume"
                            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val delta = (dragAmount / size.height) * maxVol * -2f
                            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val newVol = (currentVol + delta).toInt().coerceIn(0, maxVol)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            volume = newVol.toFloat() / maxVol
                        } else {
                            gestureType = "brightness"
                            activity?.let {
                                val params = it.window.attributes
                                val delta = (dragAmount / size.height) * -1f
                                params.screenBrightness = (params.screenBrightness + delta).coerceIn(0.01f, 1.0f)
                                it.window.attributes = params
                                brightness = params.screenBrightness
                            }
                        }
                        showGestureIndicator = true
                    },
                )
            },
    ) {
        // Secondary player (background) — SURFACE_TYPE_TEXTURE_VIEW=2 allows alpha-based swap without black flicker
        val secondary = secondaryExoPlayer
        if (secondary != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = secondary
                        useController = false
                        applyTextureViewSurface()
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }
                },
                modifier = Modifier.fillMaxSize().alpha(if (isPrimaryActive) 0f else 1f),
            )
        }

        // Primary player — TextureView (SURFACE_TYPE_TEXTURE_VIEW=2)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    applyTextureViewSurface()
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            modifier = Modifier.fillMaxSize().alpha(if (isPrimaryActive) 1f else 0f),
        )

        // x2 speed indicator — fades in on hold, fades out on release
        AnimatedVisibility(
            visible = isLongPressing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            ) {
                Text("▶▶ 2×", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Controls overlay
        if (isControlsVisible) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {

                // ── Top Bar ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))

                    val titleText = remember(item, parentItem) {
                        if (item.type == MediaType.EPISODE) {
                            val seriesName = parentItem?.title ?: "Series"
                            val seasonStr = item.parentIndexNumber?.let { "S${it.toString().padStart(2, '0')}" } ?: ""
                            val episodeStr = item.index?.let { "E${it.toString().padStart(2, '0')}" } ?: ""
                            val sep = if (seasonStr.isNotEmpty() && episodeStr.isNotEmpty()) " " else ""
                            "$seriesName - $seasonStr$sep$episodeStr - ${item.title}"
                        } else item.title
                    }
                    Text(
                        text = titleText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )

                }

                // ── Center Controls ──────────────────────────────────────────
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    // Prev episode
                    if (showNextPrev) {
                        IconButton(onClick = onPrevEpisode, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.SkipPrevious, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(36.dp))
                        }
                    }

                    // Rewind 10s
                    IconButton(onClick = {
                        exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                    }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.Replay10, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(48.dp))
                    }

                    // Play / Pause / Buffer
                    if (isBuffering) {
                        CircularProgressIndicator(modifier = Modifier.size(72.dp), color = Color.White, strokeWidth = 3.dp)
                    } else {
                        IconButton(
                            onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                            modifier = Modifier.size(80.dp),
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(72.dp),
                            )
                        }
                    }

                    // Forward 10s
                    IconButton(onClick = {
                        exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                    }, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.Default.Forward10, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(48.dp))
                    }

                    // Next episode
                    if (showNextPrev) {
                        IconButton(onClick = onNextEpisode, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(36.dp))
                        }
                    }
                }

                // ── Bottom Controls ──────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
                ) {
                    // Info row: [time]  [spacer]  [captions] [mark?] [settings]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(formatTime(currentPosition), color = Color.White, fontSize = 12.sp)
                        Text(" / ", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text(formatTime(duration), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        // Captions button
                        IconButton(onClick = { /* Subtitles */ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Subtitles, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        // Mark preview button — shown next to captions for episodes
                        if (item.type == MediaType.EPISODE) {
                            IconButton(
                                onClick = {
                                    when (markerState) {
                                        MarkerState.IDLE -> {
                                            markerStartMs = currentPosition
                                            markerState = MarkerState.MARKING
                                            stablePlaybackMs = 0L
                                        }
                                        MarkerState.MARKING -> {
                                            markerState = MarkerState.IDLE
                                            stablePlaybackMs = 0L
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    Icons.Default.BookmarkAdd,
                                    contentDescription = "Mark preview",
                                    tint = if (markerState == MarkerState.MARKING) Color.Red else Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        // Settings button
                        IconButton(onClick = { /* Settings */ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Seekbar row — full width, edges align with header 16dp padding
                    Box(
                        modifier = Modifier.fillMaxWidth().height(20.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
                        )
                        val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .background(Color(0xFF24D366), RoundedCornerShape(2.dp)),
                        )
                        if (duration > 0) {
                            Slider(
                                value = currentPosition.toFloat(),
                                onValueChange = {
                                    currentPosition = it.toLong()
                                    exoPlayer.seekTo(it.toLong())
                                    stablePlaybackMs = 0L
                                },
                                valueRange = 0f..duration.toFloat(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.Transparent,
                                    inactiveTrackColor = Color.Transparent,
                                ),
                                thumb = {
                                    Box(modifier = Modifier.size(14.dp).background(Color.White, CircleShape))
                                },
                            )
                        }
                    }
                }
            }
        }

        // Skip Intro button
        if (isInMarkerRange) {
            Button(
                onClick = { exoPlayer.seekTo(currentMarkerEndMs) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = if (isControlsVisible) 110.dp else 24.dp, end = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text("Skip Intro", fontWeight = FontWeight.SemiBold)
            }
        }

        // Marking indicator
        if (markerState == MarkerState.MARKING) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
                    .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                Text("Marking preview…", color = Color.White, fontSize = 13.sp)
            }
        }

        // Double-tap seek feedback overlay — left at 1/4 width, right at 3/4 width
        AnimatedVisibility(
            visible = showSeekFeedback,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val boxSize = 80.dp
                val xOffset = if (seekFeedbackIsRight) maxWidth * 0.75f - boxSize / 2 else maxWidth * 0.25f - boxSize / 2
                val yOffset = maxHeight / 2 - boxSize / 2
                Box(
                    modifier = Modifier
                        .size(boxSize)
                        .offset(x = xOffset, y = yOffset)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = seekFeedback + "s",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Gesture Overlay (Brightness / Volume)
        AnimatedVisibility(
            visible = showGestureIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(if (gestureType == "volume") 1f else 0f),
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp).height(120.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().fillMaxHeight(volume)
                                .align(Alignment.BottomCenter)
                                .background(Color.White, RoundedCornerShape(3.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Icon(Icons.Default.VolumeUp, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(if (gestureType == "brightness") 1f else 0f),
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp).height(120.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().fillMaxHeight(brightness)
                                .align(Alignment.BottomCenter)
                                .background(Color.White, RoundedCornerShape(3.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Icon(Icons.Default.WbSunny, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}
