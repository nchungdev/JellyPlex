package org.jellyplus.client.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.OrientationEffect
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

    val uiType = LocalUiType.current
    val mainState by mainViewModel.state.collectAsState()
    val isPlayerScreen = currentScreen is Screen.Player

    OrientationEffect(if (isPlayerScreen) ScreenOrientation.Landscape else ScreenOrientation.Portrait)

    // Redirect episode clicks to their parent series with the correct season pre-selected
    fun navigateTo(item: MediaItem) {
        if (item.type == MediaType.EPISODE && item.seriesId != null) {
            val seriesStub = MediaItem(
                id = item.seriesId,
                title = item.seriesName ?: "",
                type = MediaType.SERIES,
                backdropImageTags = item.parentBackdropImageTags,
                imageTags = null,
            )
            currentScreen = Screen.Details(seriesStub, focusSeasonId = item.seasonId)
        } else {
            currentScreen = Screen.Details(item)
        }
    }

    org.jellyplus.client.AppBackHandler(enabled = currentScreen is Screen.Details) {
        currentScreen = Screen.Home
    }

    org.jellyplus.client.AppBackHandler(enabled = currentScreen is Screen.Listing) {
        currentScreen = Screen.Home
    }

    val playerScreen = currentScreen as? Screen.Player
    org.jellyplus.client.AppBackHandler(enabled = playerScreen != null) {
        playerScreen?.let { navigateTo(it.parentItem ?: it.item) }
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            if (uiType == UiType.Desktop) {
                DesktopMainScreen(
                    viewModel = mainViewModel,
                    onMediaClick = { navigateTo(it) },
                    onViewAll = { type: MediaType, title: String ->
                        currentScreen = Screen.Listing(type, title)
                    }
                )
            } else {
                MobileMainScreen(
                    viewModel = mainViewModel,
                    sessionViewModel = sessionViewModel,
                    onMediaClick = { navigateTo(it) },
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
                    onBack = { currentScreen = Screen.Home },
                    onPlay = { },
                    onPlayEpisode = { episode, parent, playlist ->
                        currentScreen = Screen.Player(episode, playlist, parent)
                    },
                )
            } else {
                MovieDetailScreen(
                    item = screen.item,
                    onBack = { currentScreen = Screen.Home },
                    onPlay = { item ->
                        currentScreen = Screen.Player(item, listOf(item))
                    },
                )
            }
        }

        is Screen.Listing -> {
            MediaListingScreen(
                title = screen.title,
                items = if (screen.type == MediaType.MOVIE) mainState.movies else mainState.tvShows,
                baseUrl = mainState.baseUrl,
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
            }

            fun goToNext() {
                val next = currentIndex + 1
                if (next < playlist.size) {
                    currentScreen = Screen.Player(playlist[next], playlist, screen.parentItem)
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
                    onBack = { navigateTo(screen.parentItem ?: screen.item) },
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
                    onPreloadNextMeta = { playerViewModel.preloadNextEpisodeMeta() },
                    onMarkCurrentAsPlayed = { playerViewModel.markCurrentAsPlayed(screen.item.id) },
                    onToggleAutoSkip = { playerViewModel.toggleAutoSkip() },
                    onToggleAutoSkipOutro = { playerViewModel.toggleAutoSkipOutro() },
                    onToggleAutoNext = { playerViewModel.toggleAutoNext() },
                    onSeamlessNextEpisode = { goToNext() },
                )
            }
        }
    }
}
