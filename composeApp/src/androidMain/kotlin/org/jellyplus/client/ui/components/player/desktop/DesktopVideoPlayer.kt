package org.jellyplus.client.ui.components.player.desktop

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.IntroMarker
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.media.CustomHlsPlaylistParserFactory

@OptIn(UnstableApi::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun DesktopVideoPlayer(
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
    autoSkipOutro: Boolean = false,
    onToggleAutoSkipOutro: () -> Unit = {},
    autoSkipPreview: Boolean = false,
    onToggleAutoSkipPreview: () -> Unit = {},
) {
    val context = LocalContext.current
    val playFocusRequester = remember { FocusRequester() }

    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var seekValue by remember { mutableStateOf(0) }
    var showSeekIndicator by remember { mutableStateOf(false) }
    var seekIndicatorJob by remember { mutableStateOf<Job?>(null) }
    var markerState by remember { mutableStateOf(DesktopMarkerState.IDLE) }
    var markerStartMs by remember { mutableStateOf(0L) }
    var resolvedUrl by remember { mutableStateOf(url) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(url) {
        if (url.contains("/stream?static=true")) {
            try {
                val client = HttpClient()
                val body = client.get(url).bodyAsText().trim()
                client.close()
                resolvedUrl = if (body.startsWith("http")) body else url
            } catch (_: Exception) { resolvedUrl = url }
        } else resolvedUrl = url
    }

    val httpDataSourceFactory = remember(accessToken) {
        DefaultHttpDataSource.Factory().setUserAgent("JellyPlus-Desktop/1.0")
            .setDefaultRequestProperties(mapOf("X-Emby-Authorization" to "MediaBrowser Client=\"JellyPlus\", Token=\"$accessToken\""))
    }
    val hlsMediaSourceFactory = remember(httpDataSourceFactory) {
        HlsMediaSource.Factory(httpDataSourceFactory).setPlaylistParserFactory(CustomHlsPlaylistParserFactory())
    }

    val exoPlayer = remember(url, accessToken, mimeType) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory))
            .build().apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
                    override fun onPlaybackStateChanged(state: Int) { duration = this@apply.duration.coerceAtLeast(0L) }
                })
            }
    }

    LaunchedEffect(resolvedUrl) {
        val resolvedMime = if (resolvedUrl.contains(".m3u8") || resolvedUrl.contains("m3u8")) MimeTypes.APPLICATION_M3U8 else mimeType
        val mi = androidx.media3.common.MediaItem.Builder().setUri(resolvedUrl).setMimeType(resolvedMime).build()
        if (resolvedMime == MimeTypes.APPLICATION_M3U8) exoPlayer.setMediaSource(hlsMediaSourceFactory.createMediaSource(mi))
        else exoPlayer.setMediaItem(mi)
        exoPlayer.setPlaybackSpeed(playbackSpeed)
        exoPlayer.prepare()
    }

    LaunchedEffect(item.id) {
        isPlaying = true
        currentPosition = 0L
        duration = 0L
        seekValue = 0
        showSeekIndicator = false
        markerState = DesktopMarkerState.IDLE
        markerStartMs = 0L
        isControlsVisible = true
    }
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            try { playFocusRequester.requestFocus() } catch (_: Exception) {}
            delay(5000); isControlsVisible = false
        }
    }
    LaunchedEffect(isPlaying) { while (isPlaying) { currentPosition = exoPlayer.currentPosition; delay(500) } }
    LaunchedEffect(playSessionId, item.id) {
        val sessionId = playSessionId ?: return@LaunchedEffect
        onPlaybackStart(item.id, sessionId)
        while (true) { delay(10000); onPlaybackProgress(item.id, sessionId, exoPlayer.currentPosition * 10000L, !isPlaying) }
    }

    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            val sessionId = playSessionId
            if (sessionId != null) onPlaybackStopped(item.id, sessionId, exoPlayer.currentPosition * 10000L)
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    fun triggerSeek(delta: Int) {
        if (isControlsVisible) return
        seekValue += delta; showSeekIndicator = true; seekIndicatorJob?.cancel()
        seekIndicatorJob = scope.launch {
            delay(600); exoPlayer.seekTo((exoPlayer.currentPosition + seekValue * 1000L).coerceIn(0, duration))
            delay(400); showSeekIndicator = false; seekValue = 0
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (!isControlsVisible) when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            if (keyEvent.nativeKeyEvent.repeatCount > 2) {
                                seekIndicatorJob?.cancel(); showSeekIndicator = false; seekValue = 0
                                exoPlayer.setPlaybackSpeed(2f)
                            } else triggerSeek(-5)
                            true
                        }
                        Key.DirectionRight -> {
                            if (keyEvent.nativeKeyEvent.repeatCount > 2) {
                                seekIndicatorJob?.cancel(); showSeekIndicator = false; seekValue = 0
                                exoPlayer.setPlaybackSpeed(2f)
                            } else triggerSeek(10)
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown, Key.Enter, Key.DirectionCenter -> { isControlsVisible = true; true }
                        else -> false
                    } else when (keyEvent.key) {
                        Key.Back, Key.Escape -> { isControlsVisible = false; true }
                        else -> false
                    }
                } else if (keyEvent.type == KeyEventType.KeyUp) {
                    if (keyEvent.key == Key.DirectionLeft || keyEvent.key == Key.DirectionRight) { exoPlayer.setPlaybackSpeed(playbackSpeed); true }
                    else false
                } else false
            }
            .focusable()
            .focusRequester(playFocusRequester),
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false } },
            modifier = Modifier.fillMaxSize(),
        )

        // Seek indicator
        AnimatedVisibility(visible = showSeekIndicator, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(), modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val boxSize = 100.dp
                val xOffset = if (seekValue > 0) maxWidth * 0.75f - boxSize / 2 else maxWidth * 0.25f - boxSize / 2
                Box(
                    modifier = Modifier.size(boxSize).offset(x = xOffset, y = maxHeight / 2 - boxSize / 2)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (seekValue > 0) Icons.Default.Forward10 else Icons.Default.Replay10, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Text("${if (seekValue > 0) "+" else ""}$seekValue s", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2x speed badge
        if (!isControlsVisible && exoPlayer.playbackParameters.speed > 1.0f) {
            Box(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("2X Speed", color = Color(0xFF24D366), fontWeight = FontWeight.Bold)
            }
        }

        DesktopPlayerControls(
            item = item, parentItem = parentItem,
            isVisible = isControlsVisible, isPlaying = isPlaying,
            currentPosition = currentPosition, duration = duration,
            showNextPrev = showNextPrev, markerState = markerState,
            playFocusRequester = playFocusRequester,
            onBack = onBack,
            onPlayPause = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
            onRewind = { exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0)) },
            onForward = { exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration)) },
            onPrevEpisode = onPrevEpisode, onNextEpisode = onNextEpisode,
            onSeekLeft = { exoPlayer.seekTo((currentPosition - 5000).coerceAtLeast(0)) },
            onSeekRight = { exoPlayer.seekTo((currentPosition + 10000).coerceAtMost(duration)) },
            onMarkToggle = {
                when (markerState) {
                    DesktopMarkerState.IDLE -> { markerStartMs = currentPosition; markerState = DesktopMarkerState.MARKING }
                    DesktopMarkerState.MARKING -> { onSaveCustomMarker(markerStartMs, currentPosition); markerState = DesktopMarkerState.IDLE }
                }
            },
        )
    }
}
