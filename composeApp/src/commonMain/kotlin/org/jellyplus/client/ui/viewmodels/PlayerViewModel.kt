package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.IntroMarker
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.domain.usecases.*

data class PlayerState(
    val itemId: String? = null,
    val url: String? = null,
    val mimeType: String? = null,
    val accessToken: String = "",
    val playSessionId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Episode context
    val episodes: List<MediaItem> = emptyList(),
    val currentEpisodeIndex: Int = -1,
    val nextEpisodeItem: MediaItem? = null,
    val nextEpisodeConfig: PlaybackConfig? = null,
    val isPreloadingNextMeta: Boolean = false,
    // Intro Skipper markers from server
    val markers: List<IntroMarker> = emptyList(),
    // Player preferences
    val autoSkipIntro: Boolean = false,
    val autoSkipOutro: Boolean = false,
    val autoSkipRecap: Boolean = false,
    val autoSkipPreview: Boolean = false,
    val autoNext: Boolean = false,
    val seamlessTransition: Boolean = false,
)

class PlayerViewModel(
    private val resolveStreamConfigUseCase: ResolveStreamConfigUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getAccessTokenUseCase: GetAccessTokenUseCase,
    private val reportPlaybackStartUseCase: ReportPlaybackStartUseCase,
    private val reportPlaybackProgressUseCase: ReportPlaybackProgressUseCase,
    private val reportPlaybackStoppedUseCase: ReportPlaybackStoppedUseCase,
    private val markItemAsPlayedUseCase: MarkItemAsPlayedUseCase,
    private val getAutoSkipUseCase: GetAutoSkipUseCase,
    private val setAutoSkipUseCase: SetAutoSkipUseCase,
    private val getAutoNextUseCase: GetAutoNextUseCase,
    private val setAutoNextUseCase: SetAutoNextUseCase,
    private val getAutoSkipOutroUseCase: GetAutoSkipOutroUseCase,
    private val setAutoSkipOutroUseCase: SetAutoSkipOutroUseCase,
    private val getIntroMarkersUseCase: GetIntroMarkersUseCase,
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        loadAutoSkipPreference()
    }

    fun loadStreamUrl(item: MediaItem) {
        if (_state.value.itemId == item.id && _state.value.url != null) return

        viewModelScope.launch(dispatchers.main) {
            _state.value = _state.value.copy(
                itemId = item.id,
                url = null,
                mimeType = null,
                playSessionId = null,
                error = null,
                isLoading = true,
                accessToken = getAccessTokenUseCase() ?: ""
            )
            val userId = getUserIdUseCase() ?: ""
            val deviceId = "CMP-ID"

            try {
                val config = resolveStreamConfigUseCase(item, userId, deviceId)
                if (config != null) {
                    _state.value = _state.value.copy(
                        url = config.url,
                        playSessionId = config.playSessionId,
                        mimeType = config.mimeType,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "Failed to resolve stream configuration",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun setEpisodeContext(episodes: List<MediaItem>, currentIndex: Int) {
        val nextItem = if (currentIndex >= 0 && currentIndex + 1 < episodes.size) {
            episodes[currentIndex + 1]
        } else null
        _state.value = _state.value.copy(
            episodes = episodes,
            currentEpisodeIndex = currentIndex,
            nextEpisodeItem = nextItem,
            nextEpisodeConfig = null,
            isPreloadingNextMeta = false,
        )
    }

    fun loadEpisodePlaylist(episode: MediaItem) {
        val seriesId = episode.seriesId ?: return
        val seasonId = episode.seasonId ?: return
        viewModelScope.launch(dispatchers.io) {
            getEpisodesUseCase(seriesId, seasonId).onSuccess { episodes ->
                val idx = episodes.indexOfFirst { it.id == episode.id }
                if (idx >= 0) setEpisodeContext(episodes, idx)
            }
        }
    }

    fun preloadNextEpisodeMeta() {
        val nextItem = _state.value.nextEpisodeItem ?: return
        if (_state.value.isPreloadingNextMeta || _state.value.nextEpisodeConfig != null) return

        _state.value = _state.value.copy(isPreloadingNextMeta = true)
        viewModelScope.launch(dispatchers.io) {
            try {
                val userId = getUserIdUseCase() ?: ""
                val config = resolveStreamConfigUseCase(nextItem, userId, "CMP-ID")
                _state.value = _state.value.copy(nextEpisodeConfig = config, isPreloadingNextMeta = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(isPreloadingNextMeta = false)
            }
        }
    }

    fun markCurrentAsPlayed(itemId: String) {
        viewModelScope.launch(dispatchers.io) {
            try { markItemAsPlayedUseCase(itemId) } catch (_: Exception) {}
        }
    }

    fun toggleAutoSkip() {
        val next = !_state.value.autoSkipIntro
        setAutoSkipUseCase(next)
        _state.value = _state.value.copy(autoSkipIntro = next)
    }

    fun toggleAutoSkipOutro() {
        val next = !_state.value.autoSkipOutro
        setAutoSkipOutroUseCase(next)
        _state.value = _state.value.copy(autoSkipOutro = next)
    }

    fun toggleAutoNext() {
        val next = !_state.value.autoNext
        setAutoNextUseCase(next)
        _state.value = _state.value.copy(autoNext = next)
    }

    fun toggleAutoSkipRecap() {
        val next = !_state.value.autoSkipRecap
        _state.value = _state.value.copy(autoSkipRecap = next)
    }

    fun toggleAutoSkipPreview() {
        val next = !_state.value.autoSkipPreview
        _state.value = _state.value.copy(autoSkipPreview = next)
    }

    fun toggleSeamlessTransition() {
        val next = !_state.value.seamlessTransition
        _state.value = _state.value.copy(seamlessTransition = next)
    }

    fun loadMarkers(itemId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val markers = getIntroMarkersUseCase(itemId)
                _state.value = _state.value.copy(markers = markers)
            } catch (_: Exception) {}
        }
    }

    fun reportStart(itemId: String, playSessionId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                reportPlaybackStartUseCase(itemId, playSessionId)
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "reportStart failed: ${e.message}", e)
            }
        }
    }

    fun reportProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean = false
    ) {
        viewModelScope.launch(dispatchers.io) {
            try {
                reportPlaybackProgressUseCase(itemId, playSessionId, positionTicks, isPaused, isMuted)
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "reportProgress failed: ${e.message}", e)
            }
        }
    }

    fun reportStopped(itemId: String, playSessionId: String, positionTicks: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                reportPlaybackStoppedUseCase(itemId, playSessionId, positionTicks)
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "reportStopped failed: ${e.message}", e)
            }
        }
    }

    private fun loadAutoSkipPreference() {
        _state.value = _state.value.copy(
            autoSkipIntro = getAutoSkipUseCase(),
            autoSkipOutro = getAutoSkipOutroUseCase(),
            autoNext = getAutoNextUseCase(),
        )
    }
}
