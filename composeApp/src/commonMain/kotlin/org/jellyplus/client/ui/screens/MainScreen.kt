package org.jellyplus.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.OrientationEffect
import org.jellyplus.client.PlayerFullscreenEffect
import org.jellyplus.client.ScreenOrientation
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.ui.Screen
import org.jellyplus.client.ui.desktop.screens.DesktopMainScreen
import org.jellyplus.client.ui.mobile.screens.MobileMainScreen
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.MainState
import org.jellyplus.client.ui.viewmodels.PlayerViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel
import org.koin.compose.viewmodel.koinViewModel

// Custom Saver to persist MutableState<Screen> across activity recreation
private val ScreenStateSaver = Saver<MutableState<Screen>, String>(
    save = { Json.encodeToString(it.value) },
    restore = {
        mutableStateOf(
            try {
                Json.decodeFromString<Screen>(it)
            } catch (e: Exception) {
                Screen.Home
            }
        )
    }
)

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    sessionViewModel: SessionViewModel = koinViewModel(),
) {
    // Persist current screen state using rememberSaveable
    val currentScreenState = rememberSaveable(saver = ScreenStateSaver) {
        mutableStateOf<Screen>(Screen.Home)
    }
    var currentScreen by currentScreenState
    var homeTabIndex by rememberSaveable { mutableStateOf(0) }
    var detailBackTabIndex by rememberSaveable { mutableStateOf(0) }

    val uiType = LocalUiType.current
    val mainState by mainViewModel.state.collectAsState()
    val isPlayerScreen = currentScreen is Screen.Player

    OrientationEffect(
        if (uiType == UiType.Desktop || isPlayerScreen) {
            ScreenOrientation.Landscape
        } else {
            ScreenOrientation.Portrait
        }
    )
    PlayerFullscreenEffect(isPlayerScreen)

    fun seriesStubForEpisode(item: MediaItem): MediaItem? {
        return item.seriesId?.let { seriesId ->
            MediaItem(
                id = seriesId,
                title = item.seriesName ?: "",
                type = MediaType.SERIES,
                backdropImageTags = item.parentBackdropImageTags,
                imageTags = null,
            )
        }
    }

    fun playItem(item: MediaItem, backTabIndex: Int = homeTabIndex) {
        detailBackTabIndex = backTabIndex
        when {
            item.type == MediaType.EPISODE && item.seriesId != null -> {
                currentScreen = Screen.Player(item, listOf(item), parentItem = seriesStubForEpisode(item))
            }
            item.type == MediaType.MOVIE -> {
                currentScreen = Screen.Player(item, listOf(item))
            }
            else -> currentScreen = Screen.Details(item)
        }
    }

    fun navigateTo(item: MediaItem) {
        detailBackTabIndex = homeTabIndex
        currentScreen = when {
            item.type == MediaType.EPISODE && item.seriesId != null -> {
                Screen.Player(item, listOf(item), parentItem = seriesStubForEpisode(item))
            }
            item.type == MediaType.EPISODE -> Screen.Player(item, listOf(item))
            item.type == MediaType.MOVIE || item.type == MediaType.SERIES -> Screen.Details(item)
            else -> Screen.Details(item)
        }
    }

    org.jellyplus.client.AppBackHandler(enabled = currentScreen is Screen.Details) {
        homeTabIndex = detailBackTabIndex
        currentScreen = Screen.Home
    }

    org.jellyplus.client.AppBackHandler(enabled = currentScreen is Screen.Listing) {
        currentScreen = Screen.Home
    }

    org.jellyplus.client.AppBackHandler(enabled = currentScreen is Screen.Home && homeTabIndex != 0) {
        homeTabIndex = 0
    }

    // Back from player: go to series detail (with correct season) or movie detail
    fun backFromPlayer(ps: Screen.Player) {
        val parent = ps.parentItem ?: ps.item
        if (parent.type == MediaType.SERIES && ps.item.type == MediaType.EPISODE) {
            currentScreen = Screen.Details(parent, focusSeasonId = ps.item.seasonId)
        } else {
            currentScreen = Screen.Details(parent)
        }
    }

    val playerScreen = currentScreen as? Screen.Player
    org.jellyplus.client.AppBackHandler(enabled = playerScreen != null) {
        playerScreen?.let { backFromPlayer(it) }
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            if (uiType == UiType.Desktop) {
                DesktopMainScreen(
                    viewModel = mainViewModel,
                    selectedNavIndex = homeTabIndex,
                    onSelectedNavIndexChange = { homeTabIndex = it },
                    onMediaClick = { navigateTo(it) },
                    onContinueWatchingClick = { playItem(it, homeTabIndex) },
                    onViewAll = { type: MediaType, title: String ->
                        currentScreen = Screen.Listing(type, title)
                    }
                )
            } else {
                MobileMainScreen(
                    viewModel = mainViewModel,
                    sessionViewModel = sessionViewModel,
                    selectedTab = homeTabIndex.coerceIn(0, 3),
                    onSelectedTabChange = { homeTabIndex = it },
                    onMediaClick = { navigateTo(it) },
                    onContinueWatchingClick = { playItem(it, homeTabIndex) },
                    onViewAll = { type: MediaType, title: String ->
                        currentScreen = Screen.Listing(type, title)
                    }
                )
            }
        }

        is Screen.Details -> {
            if (screen.item.type == MediaType.SERIES) {
                SeriesDetailScreen(
                    item = screen.item,
                    focusSeasonId = screen.focusSeasonId,
                    recommendedItems = mainState.tvShows.filterNot { it.id == screen.item.id }.take(12),
                    onBack = {
                        homeTabIndex = detailBackTabIndex
                        currentScreen = Screen.Home
                    },
                    onPlay = { },
                    onPlayEpisode = { episode, parent, playlist ->
                        currentScreen = Screen.Player(episode, playlist, parent)
                    },
                    onRecommendedClick = { navigateTo(it) },
                )
            } else {
                MovieDetailScreen(
                    item = screen.item,
                    recommendedItems = mainState.movies.filterNot { it.id == screen.item.id }.take(12),
                    onBack = {
                        homeTabIndex = detailBackTabIndex
                        currentScreen = Screen.Home
                    },
                    onPlay = { item ->
                        currentScreen = Screen.Player(item, listOf(item))
                    },
                    onRecommendedClick = { navigateTo(it) },
                )
            }
        }

        is Screen.Listing -> {
            MediaListingScreen(
                title = screen.title,
                items = if (screen.type == MediaType.MOVIE) mainState.movies else mainState.tvShows,
                baseUrl = mainState.baseUrl,
                isLoadingMore = if (screen.type == MediaType.MOVIE) mainState.isLoadingMoreMovies else mainState.isLoadingMoreTvShows,
                hasMore = if (screen.type == MediaType.MOVIE) mainState.hasMoreMovies else mainState.hasMoreTvShows,
                onLoadMore = {
                    if (screen.type == MediaType.MOVIE) {
                        mainViewModel.loadMoreMovies()
                    } else {
                        mainViewModel.loadMoreTvShows()
                    }
                },
                onBack = { currentScreen = Screen.Home },
                onMediaClick = { navigateTo(it) }
            )
        }

        is Screen.Player -> {
            val playerViewModel: PlayerViewModel = koinViewModel()
            val playerState by playerViewModel.state.collectAsState()

            val playlist = screen.playlist
            val currentIndex = playlist.indexOfFirst { it.id == screen.item.id }

            LaunchedEffect(screen.item.id) {
                playerViewModel.loadStreamUrl(screen.item)
                playerViewModel.setEpisodeContext(playlist, currentIndex)
                playerViewModel.loadMarkers(screen.item.id)
                // Opened directly (not from series detail): load full season playlist
                if (screen.item.type == MediaType.EPISODE && playlist.size == 1) {
                    playerViewModel.loadEpisodePlaylist(screen.item)
                }
            }

            fun goToNext() {
                val next = currentIndex + 1
                if (next < playlist.size) {
                    currentScreen = Screen.Player(playlist[next], playlist, screen.parentItem)
                }
            }

            var showDelayedLoading by rememberSaveable(screen.item.id) { mutableStateOf(false) }

            LaunchedEffect(screen.item.id, playerState.url) {
                showDelayedLoading = false
                if (playerState.url == null) {
                    kotlinx.coroutines.delay(2_000)
                    if (playerState.url == null) showDelayedLoading = true
                }
            }

            playerState.url?.let { url ->
                org.jellyplus.client.ui.components.VideoPlayer(
                    item = screen.item,
                    parentItem = screen.parentItem,
                    url = url,
                    mimeType = playerState.mimeType,
                    accessToken = playerState.accessToken,
                    playSessionId = playerState.playSessionId,
                    onBack = { backFromPlayer(screen) },
                    showNextPrev = playlist.size > 1,
                    onPlaybackStart = { itemId, sessionId ->
                        playerViewModel.reportStart(itemId, sessionId)
                    },
                    onPlaybackProgress = { itemId, sessionId, pos, paused ->
                        playerViewModel.reportProgress(itemId, sessionId, pos, paused)
                    },
                    onPlaybackStopped = { itemId, sessionId, pos ->
                        playerViewModel.reportStopped(itemId, sessionId, pos)
                    },
                    onNextEpisode = { goToNext() },
                    onPrevEpisode = {
                        val prev = currentIndex - 1
                        if (prev >= 0) {
                            currentScreen = Screen.Player(playlist[prev], playlist, screen.parentItem)
                        }
                    },
                    uiType = uiType,
                    markers = playerState.markers,
                    nextEpisodeConfig = playerState.nextEpisodeConfig,
                    autoSkipIntro = playerState.autoSkipIntro,
                    autoSkipOutro = playerState.autoSkipOutro,
                    autoNext = playerState.autoNext,
                    autoPictureInPicture = playerState.autoPictureInPicture,
                    playbackSpeed = playerState.playbackSpeed,
                    onPreloadNextMeta = { playerViewModel.preloadNextEpisodeMeta() },
                    onMarkCurrentAsPlayed = { playerViewModel.markCurrentAsPlayed(screen.item.id) },
                    onToggleAutoSkip = { playerViewModel.toggleAutoSkip() },
                    onToggleAutoSkipOutro = { playerViewModel.toggleAutoSkipOutro() },
                    onToggleAutoNext = { playerViewModel.toggleAutoNext() },
                    onToggleAutoPictureInPicture = { playerViewModel.toggleAutoPictureInPicture() },
                    onSpeedChange = { playerViewModel.setPlaybackSpeed(it) },
                    onSeamlessNextEpisode = { goToNext() },
                    originalAudioLanguage = playerState.originalAudioLanguage,
                )
            } ?: PlayerResolvingScreen(
                item = screen.item,
                parentItem = screen.parentItem,
                showLoading = showDelayedLoading,
                onBack = { backFromPlayer(screen) },
            )
        }
    }
}

@Composable
private fun PlayerResolvingScreen(
    item: MediaItem,
    parentItem: MediaItem?,
    showLoading: Boolean,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(24.dp))
            Text(
                text = buildPlayerShellTitle(item, parentItem),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (showLoading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(14.dp))
                Text(
                    "Loading stream...",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun buildPlayerShellTitle(item: MediaItem, parentItem: MediaItem?): String {
    if (item.type != MediaType.EPISODE) return item.title
    val series = parentItem?.title ?: item.seriesName
    val season = item.parentIndexNumber?.let { "S${it.toString().padStart(2, '0')}" }.orEmpty()
    val episode = item.index?.let { "E${it.toString().padStart(2, '0')}" }.orEmpty()
    val code = listOf(season, episode).filter { it.isNotEmpty() }.joinToString(" ")
    return listOfNotNull(series, code.takeIf { it.isNotEmpty() }, item.title)
        .joinToString(" - ")
}
