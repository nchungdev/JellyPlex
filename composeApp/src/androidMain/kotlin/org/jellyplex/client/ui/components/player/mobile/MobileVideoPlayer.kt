package org.jellyplex.client.ui.components.player.mobile

import android.annotation.SuppressLint
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import org.jellyplex.client.data.remote.IntroMarker
import org.jellyplex.client.domain.models.MediaType
import org.jellyplex.client.media.CustomHlsPlaylistParserFactory

@OptIn(UnstableApi::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MobileVideoPlayer(
    item: org.jellyplex.client.domain.models.MediaItem,
    parentItem: org.jellyplex.client.domain.models.MediaItem? = null,
    url: String,
    accessToken: String,
    playSessionId: String? = null,
    mimeType: String?,
    markers: List<IntroMarker>,
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
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val audioManager = remember { context.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager }

    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    // Gesture States
    var brightness by remember { mutableStateOf(activity?.window?.attributes?.screenBrightness ?: 0.5f) }
    var volume by remember {
        mutableStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() /
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
        )
    }
    var showGestureIndicator by remember { mutableStateOf(false) }
    var gestureType by remember { mutableStateOf("") }

    val exoPlayer =
        remember {
            val httpDataSourceFactory =
                DefaultHttpDataSource.Factory()
                    .setUserAgent("Mozilla/5.0")
                    .setDefaultRequestProperties(
                        mapOf("X-Emby-Authorization" to "MediaBrowser Client=\"JellyPlex\", Token=\"$accessToken\""),
                    )

            val mediaSourceFactory =
                DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(httpDataSourceFactory)

            val hlsMediaSourceFactory =
                HlsMediaSource.Factory(httpDataSourceFactory)
                    .setPlaylistParserFactory(CustomHlsPlaylistParserFactory())

            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    val resolvedMimeType =
                        when {
                            mimeType?.lowercase()?.contains("hls") == true -> MimeTypes.APPLICATION_M3U8
                            mimeType?.lowercase()?.contains("m3u8") == true -> MimeTypes.APPLICATION_M3U8
                            mimeType?.lowercase()?.contains("m3u") == true -> MimeTypes.APPLICATION_M3U8
                            url.contains(".m3u8") || url.contains(".m3u") || url.contains("hls") -> MimeTypes.APPLICATION_M3U8
                            else -> mimeType
                        }

                    if (resolvedMimeType == MimeTypes.APPLICATION_M3U8) {
                        val mediaItem = MediaItem.Builder()
                            .setUri(url)
                            .setMimeType(resolvedMimeType)
                            .build()
                        setMediaSource(hlsMediaSourceFactory.createMediaSource(mediaItem))
                    } else {
                        val mediaItem =
                            MediaItem.Builder()
                                .setUri(url)
                                .setMimeType(resolvedMimeType)
                                .build()
                        setMediaItem(mediaItem)
                    }

                    setPlaybackSpeed(playbackSpeed)
                    prepare()
                    playWhenReady = true

                    addListener(
                        object : Player.Listener {
                            override fun onIsPlayingChanged(playing: Boolean) {
                                isPlaying = playing
                            }

                            override fun onPlaybackStateChanged(state: Int) {
                                duration = this@apply.duration.coerceAtLeast(0L)
                            }
                        },
                    )
                }
        }

    LaunchedEffect(isControlsVisible, isPlaying) {
        if (isControlsVisible && isPlaying) {
            delay(5000)
            isControlsVisible = false
        }
    }

    LaunchedEffect(showGestureIndicator) {
        if (showGestureIndicator) {
            delay(2000)
            showGestureIndicator = false
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition
            delay(1000)
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
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            val sessionId = playSessionId
            if (sessionId != null) {
                onPlaybackStopped(item.id, sessionId, exoPlayer.currentPosition * 10000L)
            }
            exoPlayer.release()
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { isControlsVisible = !isControlsVisible },
                        onDoubleTap = { offset ->
                            val isLeft = offset.x < size.width / 2
                            if (isLeft) {
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                            } else {
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                            }
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { showGestureIndicator = true },
                        onDragEnd = { /* auto hide via LaunchedEffect */ },
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
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams =
                        android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (isControlsVisible) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                // Top Bar
                Row(
                    modifier =
                        Modifier
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

                    Text(
                        text = titleText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    // Subtitle and Settings at top right
                    IconButton(onClick = { /* Subtitles */ }) {
                        Icon(Icons.Default.Subtitles, null, tint = Color.White)
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                    }
                }

                // Center Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                ) {
                    IconButton(onClick = {
                        exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                    }, modifier = Modifier.size(64.dp)) {
                        Icon(
                            Icons.Default.Replay10,
                            null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(48.dp),
                        )
                    }
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
                    IconButton(onClick = {
                        exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(duration))
                    }, modifier = Modifier.size(64.dp)) {
                        Icon(
                            Icons.Default.Forward10,
                            null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }

                // Bottom Controls
                Column(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Time text on the left
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 12.sp,
                            )
                            Text(
                                " / ",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                            )
                            Text(
                                formatTime(duration),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            // Custom Track - Background
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(if (isControlsVisible) 4.dp else 2.dp)
                                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
                            )

                            // Custom Track - Progress
                            val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(
                                0f,
                                1f
                            ) else 0f
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(progress)
                                        .height(if (isControlsVisible) 4.dp else 2.dp)
                                        .background(Color(0xFF24D366), RoundedCornerShape(2.dp)),
                            )

                            // Invisible Slider
                            if (isControlsVisible && duration > 0) {
                                Slider(
                                    value = currentPosition.toFloat(),
                                    onValueChange = {
                                        currentPosition = it.toLong()
                                        exoPlayer.seekTo(it.toLong())
                                    },
                                    valueRange = 0f..duration.toFloat(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                        SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color.Transparent,
                                            inactiveTrackColor = Color.Transparent,
                                        ),
                                )
                            }
                        }
                    }

                    if (isControlsVisible && showNextPrev) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(onClick = onPrevEpisode) {
                                Icon(Icons.Default.SkipPrevious, null, tint = Color.White)
                            }
                            IconButton(onClick = onNextEpisode) {
                                Icon(Icons.Default.SkipNext, null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Gesture Overlay (Brightness/Volume)
        AnimatedVisibility(
            visible = showGestureIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Volume Bar (Left)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(if (gestureType == "volume") 1f else 0f),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(6.dp)
                                .height(120.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp)),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(volume)
                                    .align(Alignment.BottomCenter)
                                    .background(Color.White, RoundedCornerShape(3.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Icon(
                        Icons.Default.VolumeUp,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Brightness Bar (Right)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(if (gestureType == "brightness") 1f else 0f),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(6.dp)
                                .height(120.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp)),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(brightness)
                                    .align(Alignment.BottomCenter)
                                    .background(Color.White, RoundedCornerShape(3.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Icon(
                        Icons.Default.WbSunny,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
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
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
