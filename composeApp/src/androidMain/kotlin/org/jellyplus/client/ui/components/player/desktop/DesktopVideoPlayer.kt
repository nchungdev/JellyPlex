package org.jellyplus.client.ui.components.player.desktop

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.media.CustomHlsPlaylistParserFactory

private enum class DesktopMarkerState { IDLE, MARKING }

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
) {
    val context = LocalContext.current
    val playFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    // Seek UI state
    var seekValue by remember { mutableStateOf(0) }
    var showSeekIndicator by remember { mutableStateOf(false) }
    var seekIndicatorJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    // Custom marker state
    var markerState by remember { mutableStateOf(DesktopMarkerState.IDLE) }
    var markerStartMs by remember { mutableStateOf(0L) }

    var resolvedUrl by remember { mutableStateOf(url) }
    var isResolving by remember { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (url.contains("/stream?static=true")) {
            isResolving = true
            try {
                val client = HttpClient()
                val response = client.get(url)
                val body = response.bodyAsText().trim()
                client.close()

                if (body.startsWith("http")) {
                    resolvedUrl = body
                } else {
                    resolvedUrl = url
                }
            } catch (e: Exception) {
                resolvedUrl = url
            } finally {
                isResolving = false
            }
        } else {
            resolvedUrl = url
        }
    }

    val httpDataSourceFactory = remember(accessToken) {
        DefaultHttpDataSource.Factory()
            .setUserAgent("JellyPlus-Desktop/1.0")
            .setDefaultRequestProperties(mapOf("X-Emby-Authorization" to "MediaBrowser Client=\"JellyPlus\", Token=\"$accessToken\""))
    }

    val hlsMediaSourceFactory = remember(httpDataSourceFactory) {
        HlsMediaSource.Factory(httpDataSourceFactory)
            .setPlaylistParserFactory(CustomHlsPlaylistParserFactory())
    }

    val exoPlayer = remember {
        val mediaSourceFactory = DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        duration = this@apply.duration.coerceAtLeast(0L)
                    }
                })
            }
    }

    LaunchedEffect(resolvedUrl) {
        val resolvedMimeType = when {
            resolvedUrl.contains(".m3u8") || resolvedUrl.contains("m3u8") -> MimeTypes.APPLICATION_M3U8
            else -> mimeType
        }

        val mediaItem = androidx.media3.common.MediaItem.Builder()
            .setUri(resolvedUrl)
            .setMimeType(resolvedMimeType)
            .build()

        if (resolvedMimeType == MimeTypes.APPLICATION_M3U8) {
            exoPlayer.setMediaSource(hlsMediaSourceFactory.createMediaSource(mediaItem))
        } else {
            exoPlayer.setMediaItem(mediaItem)
        }

        exoPlayer.setPlaybackSpeed(playbackSpeed)
        exoPlayer.prepare()
    }

    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            try {
                playFocusRequester.requestFocus()
            } catch (_: Exception) {
            }
            delay(5000)
            isControlsVisible = false
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition
            delay(500)
        }
    }

    // Playback Reporting Loop
    LaunchedEffect(playSessionId, item.id) {
        val sessionId = playSessionId ?: return@LaunchedEffect
        onPlaybackStart(item.id, sessionId)

        while (true) {
            delay(10000) // Report every 10 seconds
            onPlaybackProgress(
                item.id,
                sessionId,
                exoPlayer.currentPosition * 10000L,
                !isPlaying
            )
        }
    }

    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            val sessionId = playSessionId
            if (sessionId != null) {
                onPlaybackStopped(item.id, sessionId, exoPlayer.currentPosition * 10000L)
            }
            exoPlayer.release()
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun triggerSeek(delta: Int) {
        if (isControlsVisible) return
        seekValue += delta
        showSeekIndicator = true
        seekIndicatorJob?.cancel()
        seekIndicatorJob = scope.launch {
            delay(600) // Shorter delay for more responsive feel
            exoPlayer.seekTo((exoPlayer.currentPosition + seekValue * 1000L).coerceIn(0, duration))
            delay(400)
            showSeekIndicator = false
            seekValue = 0
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (!isControlsVisible) {
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                triggerSeek(-5)
                                if (keyEvent.nativeKeyEvent.repeatCount > 2) {
                                    exoPlayer.setPlaybackSpeed(2.0f)
                                }
                                true
                            }
                            Key.DirectionRight -> {
                                triggerSeek(10)
                                if (keyEvent.nativeKeyEvent.repeatCount > 2) {
                                    exoPlayer.setPlaybackSpeed(2.0f)
                                }
                                true
                            }
                            Key.DirectionUp, Key.DirectionDown, Key.Enter, Key.DirectionCenter -> {
                                isControlsVisible = true
                                true
                            }
                            else -> false
                        }
                    } else {
                        when (keyEvent.key) {
                            Key.Back, Key.Escape -> {
                                isControlsVisible = false
                                true
                            }
                            else -> false
                        }
                    }
                } else if (keyEvent.type == KeyEventType.KeyUp) {
                    if (keyEvent.key == Key.DirectionLeft || keyEvent.key == Key.DirectionRight) {
                        exoPlayer.setPlaybackSpeed(playbackSpeed)
                        true
                    } else false
                } else false
            }
            .focusable(),
    ) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false } },
            modifier = Modifier.fillMaxSize(),
        )

        // Seek Indicator Overlay — positioned at 3/4 (forward) or 1/4 (rewind) of screen width
        AnimatedVisibility(
            visible = showSeekIndicator,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val boxSize = 100.dp
                val xOffset = if (seekValue > 0) maxWidth * 0.75f - boxSize / 2 else maxWidth * 0.25f - boxSize / 2
                val yOffset = maxHeight / 2 - boxSize / 2
                Box(
                    modifier = Modifier
                        .size(boxSize)
                        .offset(x = xOffset, y = yOffset)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (seekValue > 0) Icons.Default.Forward10 else Icons.Default.Replay10,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = "${if (seekValue > 0) "+" else ""}$seekValue s",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Speed Indicator
        if (!isControlsVisible && exoPlayer.playbackParameters.speed > 1.0f) {
             Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("2X Speed", color = Color(0xFF24D366), fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(visible = isControlsVisible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 48.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DesktopPlayerIconButton(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = onBack,
                        modifier = Modifier.focusProperties {
                            down = playFocusRequester
                        }
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    val titleText = remember(item, parentItem) {
                        if (item.type == MediaType.EPISODE) {
                            val seriesName = parentItem?.title ?: "Series"
                            val seasonStr = if (item.parentIndexNumber != null) "S${
                                item.parentIndexNumber.toString().padStart(2, '0')
                            }" else ""
                            val episodeStr =
                                if (item.index != null) "E${item.index.toString().padStart(2, '0')}" else ""
                            val sep = if (seasonStr.isNotEmpty() && episodeStr.isNotEmpty()) " " else ""
                            "$seriesName - $seasonStr$sep$episodeStr - ${item.title}"
                        } else {
                            item.title
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titleText,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            item.genres?.joinToString(" • ") ?: "Action • Drama",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Center Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    if (showNextPrev) {
                        DesktopPlayerIconButton(Icons.Default.SkipPrevious, size = 56.dp, iconSize = 32.dp) {
                            onPrevEpisode()
                        }
                    }

                    DesktopPlayerIconButton(Icons.Default.Replay10, size = 64.dp, iconSize = 40.dp) {
                        exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                    }

                    DesktopPlayerButton(
                        onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                        size = 96.dp,
                        modifier = Modifier.focusRequester(playFocusRequester)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            null,
                            tint = Color.Black,
                            modifier = Modifier.size(56.dp),
                        )
                    }

                    DesktopPlayerIconButton(Icons.Default.Forward10, size = 64.dp, iconSize = 40.dp) {
                        exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                    }

                    if (showNextPrev) {
                        DesktopPlayerIconButton(Icons.Default.SkipNext, size = 56.dp, iconSize = 32.dp) {
                            onNextEpisode()
                        }
                    }
                }

                // Bottom Controls — info row above seekbar, edges align with header 48dp padding
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 48.dp, vertical = 64.dp),
                ) {
                    // Info row: [time]  [spacer]  [captions] [mark?] [settings]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(formatTime(currentPosition), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text(" / ", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
                        Text(formatTime(duration), color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        DesktopPlayerIconButton(Icons.Default.Subtitles, size = 40.dp, iconSize = 22.dp) { /* Subtitles */ }
                        if (item.type == MediaType.EPISODE) {
                            DesktopPlayerIconButton(
                                icon = Icons.Default.BookmarkAdd,
                                size = 40.dp,
                                iconSize = 22.dp,
                                tint = if (markerState == DesktopMarkerState.MARKING) Color.Red else Color.White,
                            ) {
                                when (markerState) {
                                    DesktopMarkerState.IDLE -> {
                                        markerStartMs = currentPosition
                                        markerState = DesktopMarkerState.MARKING
                                    }
                                    DesktopMarkerState.MARKING -> {
                                        onSaveCustomMarker(markerStartMs, currentPosition)
                                        markerState = DesktopMarkerState.IDLE
                                    }
                                }
                            }
                        }
                        DesktopPlayerIconButton(Icons.Default.Settings, size = 40.dp, iconSize = 22.dp) { /* Settings */ }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seekbar row — full width
                    var isSeekbarFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .onFocusChanged { isSeekbarFocused = it.isFocused }
                            .focusable()
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionLeft -> { exoPlayer.seekTo((currentPosition - 5000).coerceAtLeast(0)); true }
                                        Key.DirectionRight -> { exoPlayer.seekTo((currentPosition + 10000).coerceAtMost(duration)); true }
                                        else -> false
                                    }
                                } else false
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isSeekbarFocused) 10.dp else 6.dp)
                                .background(
                                    if (isSeekbarFocused) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(5.dp),
                                ),
                        )
                        val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(if (isSeekbarFocused) 10.dp else 6.dp)
                                    .background(
                                        if (isSeekbarFocused) Color.White else Color(0xFF24D366),
                                        RoundedCornerShape(5.dp),
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopPlayerIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    tint: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(size)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .background(
                if (isFocused) Color.White else Color.White.copy(alpha = 0.1f),
                CircleShape
            )
            .border(
                width = 2.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            null,
            tint = if (isFocused) Color.Black else tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun DesktopPlayerButton(
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(size)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .background(
                if (isFocused) Color(0xFF24D366) else Color.White.copy(alpha = 0.2f),
                CircleShape
            )
            .border(
                width = if (isFocused) 4.dp else 0.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
