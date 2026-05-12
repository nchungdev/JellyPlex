package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.domain.usecases.GetAccessTokenUseCase
import org.jellyplus.client.domain.usecases.GetAutoSkipUseCase
import org.jellyplus.client.domain.usecases.GetUserIdUseCase
import org.jellyplus.client.domain.usecases.MarkItemAsPlayedUseCase
import org.jellyplus.client.domain.usecases.ReportPlaybackProgressUseCase
import org.jellyplus.client.domain.usecases.ReportPlaybackStartUseCase
import org.jellyplus.client.domain.usecases.ReportPlaybackStoppedUseCase
import org.jellyplus.client.domain.usecases.ResolveStreamConfigUseCase
import org.jellyplus.client.domain.usecases.SaveCustomMarkerUseCase
import org.jellyplus.client.domain.usecases.SetAutoSkipUseCase

data class PlayerState(
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
    // Player preferences
    val autoSkipIntro: Boolean = false,
    // Custom markers in RAM: list of (startMs, endMs)
    val customMarkers: List<Pair<Long, Long>> = emptyList(),
)

class PlayerViewModel(
    private val resolveStreamConfigUseCase: ResolveStreamConfigUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getAccessTokenUseCase: GetAccessTokenUseCase,
    private val reportPlaybackStartUseCase: ReportPlaybackStartUseCase,
    private val reportPlaybackProgressUseCase: ReportPlaybackProgressUseCase,
    private val reportPlaybackStoppedUseCase: ReportPlaybackStoppedUseCase,
    private val markItemAsPlayedUseCase: MarkItemAsPlayedUseCase,
    private val saveCustomMarkerUseCase: SaveCustomMarkerUseCase,
    private val getAutoSkipUseCase: GetAutoSkipUseCase,
    private val setAutoSkipUseCase: SetAutoSkipUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        loadAutoSkipPreference()
    }

    fun loadStreamUrl(item: MediaItem) {
        if (_state.value.url != null) return

        viewModelScope.launch(dispatchers.main) {
            _state.value = _state.value.copy(isLoading = true, accessToken = getAccessTokenUseCase() ?: "")
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

    fun addCustomMarker(startMs: Long, endMs: Long, itemId: String) {
        _state.value = _state.value.copy(
            customMarkers = _state.value.customMarkers + (startMs to endMs)
        )
        viewModelScope.launch(dispatchers.io) {
            try { saveCustomMarkerUseCase(itemId, startMs, endMs) } catch (_: Exception) {}
        }
    }

    fun toggleAutoSkip() {
        val next = !_state.value.autoSkipIntro
        setAutoSkipUseCase(next)
        _state.value = _state.value.copy(autoSkipIntro = next)
    }

    fun reportStart(itemId: String, playSessionId: String) {
        viewModelScope.launch(dispatchers.io) {
            try { reportPlaybackStartUseCase(itemId, playSessionId) } catch (_: Exception) {}
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
            } catch (_: Exception) {}
        }
    }

    fun reportStopped(itemId: String, playSessionId: String, positionTicks: Long) {
        viewModelScope.launch(dispatchers.io) {
            try { reportPlaybackStoppedUseCase(itemId, playSessionId, positionTicks) } catch (_: Exception) {}
        }
    }

    private fun loadAutoSkipPreference() {
        _state.value = _state.value.copy(autoSkipIntro = getAutoSkipUseCase())
    }
}
