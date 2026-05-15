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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
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
import org.jellyplus.client.ui.components.player.PlayerAudioDialog
import org.jellyplus.client.ui.components.player.PlayerCaptionDialog
import org.jellyplus.client.ui.components.player.PlayerSettingsPopup

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
    seekBackSeconds: Int = 5,
    seekForwardSeconds: Int = 10,
    showGestureHints: Boolean = true,
    showNextPrev: Boolean = false,
    onNextEpisode: () -> Unit = {},
    onPrevEpisode: () -> Unit = {},
    nextEpisodeConfig: PlaybackConfig? = null,
    autoSkipIntro: Boolean = false,
    onPreloadNextMeta: () -> Unit = {},
    onMarkCurrentAsPlayed: () -> Unit = {},
    onToggleAutoSkip: () -> Unit = {},
    onSeamlessNextEpisode: () -> Unit = {},
    autoNext: Boolean = false,
    autoPictureInPicture: Boolean = false,
    onToggleAutoNext: () -> Unit = {},
    onToggleAutoPictureInPicture: () -> Unit = {},
    onSpeedChange: (Float) -> Unit = {},
    autoSkipOutro: Boolean = false,
    onToggleAutoSkipOutro: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val playFocusRequester = remember { FocusRequester() }

    var isControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var currentSpeed by remember { mutableStateOf(playbackSpeed) }
    var seekValue by remember { mutableStateOf(0) }
    var showSeekIndicator by remember { mutableStateOf(false) }
    var showSettingsPopup by remember { mutableStateOf(false) }
    var showCaptionDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var seekIndicatorJob by remember { mutableStateOf<Job?>(null) }
    var availableTextTracks by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var availableTextTrackLanguages by remember { mutableStateOf<List<String?>>(emptyList()) }
    var selectedTextTrackIndex by remember { mutableStateOf(-1) }
    var availableAudioTracks by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var availableAudioTrackLanguages by remember { mutableStateOf<List<String?>>(emptyList()) }
    var selectedAudioTrackIndex by remember { mutableStateOf(0) }
    
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
    val trackSelector = remember { DefaultTrackSelector(context) }

    val exoPlayer = remember(url, accessToken, mimeType) {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(httpDataSourceFactory))
            .setTrackSelector(trackSelector)
            .build().apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
                    override fun onPlaybackStateChanged(state: Int) { duration = this@apply.duration.coerceAtLeast(0L) }
                    override fun onTracksChanged(tracks: Tracks) {
                        val textTracks = mutableListOf<Pair<Int, String>>()
                        val textLanguages = mutableListOf<String?>()
                        val audioTracks = mutableListOf<Pair<Int, String>>()
                        val audioLanguages = mutableListOf<String?>()
                        var textIndex = 0
                        var audioIndex = 0
                        for (group in tracks.groups) {
                            when (group.type) {
                                C.TRACK_TYPE_TEXT -> for (i in 0 until group.length) {
                                    val format = group.getTrackFormat(i)
                                    textTracks += textIndex++ to (format.label?.takeIf { it.isNotBlank() } ?: format.language?.takeIf { it.isNotBlank() } ?: "Sub $textIndex")
                                    textLanguages += format.language
                                }
                                C.TRACK_TYPE_AUDIO -> for (i in 0 until group.length) {
                                    val format = group.getTrackFormat(i)
                                    audioTracks += audioIndex++ to (format.label?.takeIf { it.isNotBlank() } ?: format.language?.takeIf { it.isNotBlank() } ?: "Audio $audioIndex")
                                    audioLanguages += format.language
                                }
                            }
                        }
                        availableTextTracks = textTracks
                        availableTextTrackLanguages = textLanguages
                        availableAudioTracks = audioTracks
                        availableAudioTrackLanguages = audioLanguages
                    }
                })
            }
    }

    LaunchedEffect(resolvedUrl) {
        val resolvedMime = if (resolvedUrl.contains(".m3u8") || resolvedUrl.contains("m3u8")) MimeTypes.APPLICATION_M3U8 else mimeType
        val mi = androidx.media3.common.MediaItem.Builder().setUri(resolvedUrl).setMimeType(resolvedMime).build()
        if (resolvedMime == MimeTypes.APPLICATION_M3U8) exoPlayer.setMediaSource(hlsMediaSourceFactory.createMediaSource(mi))
        else exoPlayer.setMediaItem(mi)
        exoPlayer.setPlaybackSpeed(currentSpeed)
        exoPlayer.prepare()
    }

    LaunchedEffect(item.id) {
        isPlaying = true
        currentPosition = 0L
        duration = 0L
        seekValue = 0
        showSeekIndicator = false
        selectedTextTrackIndex = -1
        selectedAudioTrackIndex = 0
        
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
        activity?.window?.let { window ->
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowCompat.getInsetsController(window, window.decorView).apply {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val sessionId = playSessionId
            if (sessionId != null) onPlaybackStopped(item.id, sessionId, exoPlayer.currentPosition * 10000L)
            activity?.window?.let { window ->
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
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
                            } else triggerSeek(-seekBackSeconds.coerceAtLeast(1))
                            true
                        }
                        Key.DirectionRight -> {
                            if (keyEvent.nativeKeyEvent.repeatCount > 2) {
                                seekIndicatorJob?.cancel(); showSeekIndicator = false; seekValue = 0
                                exoPlayer.setPlaybackSpeed(2f)
                            } else triggerSeek(seekForwardSeconds.coerceAtLeast(1))
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown, Key.Enter, Key.DirectionCenter -> { isControlsVisible = true; true }
                        else -> false
                    } else when (keyEvent.key) {
                        Key.Back, Key.Escape -> { isControlsVisible = false; true }
                        else -> false
                    }
                } else if (keyEvent.type == KeyEventType.KeyUp) {
                    if (keyEvent.key == Key.DirectionLeft || keyEvent.key == Key.DirectionRight) { exoPlayer.setPlaybackSpeed(currentSpeed); true }
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
                Text("2X Speed", color = Color(0xFF00D4A8), fontWeight = FontWeight.Bold)
            }
        }

        DesktopPlayerControls(
            item = item, parentItem = parentItem,
            isVisible = isControlsVisible, isPlaying = isPlaying,
            currentPosition = currentPosition, duration = duration,
            showNextPrev = showNextPrev,
            playFocusRequester = playFocusRequester,
            onBack = onBack,
            onPlayPause = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
            onRewind = { exoPlayer.seekTo((exoPlayer.currentPosition - seekBackSeconds.coerceAtLeast(1) * 1000L).coerceAtLeast(0)) },
            onForward = { exoPlayer.seekTo((exoPlayer.currentPosition + seekForwardSeconds.coerceAtLeast(1) * 1000L).coerceAtMost(duration)) },
            onPrevEpisode = onPrevEpisode, onNextEpisode = onNextEpisode,
            onSeekLeft = { exoPlayer.seekTo((currentPosition - seekBackSeconds.coerceAtLeast(1) * 1000L).coerceAtLeast(0)) },
            onSeekRight = { exoPlayer.seekTo((currentPosition + seekForwardSeconds.coerceAtLeast(1) * 1000L).coerceAtMost(duration)) },
            selectedTextTrackIndex = selectedTextTrackIndex,
            onShowCaptionDialog = { showCaptionDialog = true },
            onShowAudioDialog = { showAudioDialog = true },
            onMoreClick = { showSettingsPopup = true },
        )

        if (showCaptionDialog) PlayerCaptionDialog(
            availableTextTracks = availableTextTracks,
            selectedTextTrackIndex = selectedTextTrackIndex,
            onSelectOff = {
                selectedTextTrackIndex = -1
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .setPreferredTextLanguage(null)
                        .build()
                )
                showCaptionDialog = false
            },
            onSelectTrack = { index ->
                selectedTextTrackIndex = index
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .setPreferredTextLanguage(availableTextTrackLanguages.getOrNull(index))
                        .build()
                )
                showCaptionDialog = false
            },
            onDismiss = { showCaptionDialog = false },
        )

        if (showAudioDialog) PlayerAudioDialog(
            availableAudioTracks = availableAudioTracks,
            selectedAudioTrackIndex = selectedAudioTrackIndex,
            onSelectTrack = { index ->
                selectedAudioTrackIndex = index
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                        .setPreferredAudioLanguage(availableAudioTrackLanguages.getOrNull(index))
                        .build()
                )
                showAudioDialog = false
            },
            onDismiss = { showAudioDialog = false },
        )

        if (showSettingsPopup) PlayerSettingsPopup(
            autoSkipIntro = autoSkipIntro,
            autoSkipOutro = autoSkipOutro,
            autoNext = autoNext,
            autoPictureInPicture = autoPictureInPicture,
            isEpisode = item.type == MediaType.EPISODE,
            currentSpeed = currentSpeed,
            onToggleAutoSkip = onToggleAutoSkip,
            onToggleAutoSkipOutro = onToggleAutoSkipOutro,
            onToggleAutoNext = onToggleAutoNext,
            onToggleAutoPictureInPicture = onToggleAutoPictureInPicture,
            onSpeedChange = { speed ->
                currentSpeed = speed
                exoPlayer.setPlaybackSpeed(speed)
                onSpeedChange(speed)
            },
            onDismiss = { showSettingsPopup = false },
        )
    }
}
