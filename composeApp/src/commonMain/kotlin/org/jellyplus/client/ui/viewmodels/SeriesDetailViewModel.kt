package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.usecases.GetBaseUrlUseCase
import org.jellyplus.client.domain.usecases.GetEpisodesUseCase
import org.jellyplus.client.domain.usecases.GetSeasonsUseCase

data class SeriesDetailState(
    val seriesId: String? = null,
    val seasons: List<MediaItem> = emptyList(),
    val episodes: List<MediaItem> = emptyList(),
    val selectedSeason: MediaItem? = null,
    val selectedTabIndex: Int = 0, // 0: Seasons, 1: Overview
    val baseUrl: String = "",
    val isLoadingSeasons: Boolean = false,
    val isLoadingEpisodes: Boolean = false,
    val error: String? = null,
    val scrollPosition: Int = 0
)

class SeriesDetailViewModel(
    private val getSeasonsUseCase: GetSeasonsUseCase,
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(SeriesDetailState())
    val state: StateFlow<SeriesDetailState> = _state.asStateFlow()

    // In-memory cache for episodes: Map<SeasonId, List<Episode>>
    private val episodesCache = mutableMapOf<String, List<MediaItem>>()

    private var loadSeriesJob: Job? = null
    private var loadEpisodesJob: Job? = null

    fun loadSeriesDetails(seriesId: String, focusSeasonId: String? = null) {
        // Luôn cập nhật baseUrl mới nhất từ session
        val currentBaseUrl = getBaseUrlUseCase()
        if (_state.value.baseUrl != currentBaseUrl) {
            _state.value = _state.value.copy(baseUrl = currentBaseUrl)
        }

        if (_state.value.seriesId == seriesId && _state.value.seasons.isNotEmpty()) {
            // Series already loaded — still honour focusSeasonId if provided
            if (focusSeasonId != null) {
                val target = _state.value.seasons.find { it.id == focusSeasonId }
                if (target != null && target.id != _state.value.selectedSeason?.id) {
                    selectSeason(target)
                }
            }
            return
        }

        loadSeriesJob?.cancel()
        loadSeriesJob = viewModelScope.launch(dispatchers.io) {
            _state.value = _state.value.copy(
                seriesId = seriesId,
                isLoadingSeasons = true
            )
            val result = getSeasonsUseCase(seriesId)

            result.onSuccess { seasons ->
                val lastSelectedSeasonId = _state.value.selectedSeason?.id
                // focusSeasonId takes highest priority (opened from episode context)
                val seasonToSelect = seasons.find { it.id == focusSeasonId }
                    ?: seasons.find { it.id == lastSelectedSeasonId }
                    ?: seasons.find { !it.isPlayed }
                    ?: seasons.firstOrNull()

                _state.value = _state.value.copy(
                    seasons = seasons,
                    selectedSeason = seasonToSelect,
                    isLoadingSeasons = false
                )

                if (seasonToSelect != null) {
                    loadEpisodes(seriesId, seasonToSelect.id)
                }
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoadingSeasons = false, error = e.message)
            }
        }
    }

    fun selectTabIndex(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }

    fun selectSeason(season: MediaItem) {
        if (_state.value.selectedSeason?.id == season.id) return

        val seriesId = _state.value.seriesId ?: return
        _state.value = _state.value.copy(selectedSeason = season)
        loadEpisodes(seriesId, season.id)
    }

    private fun loadEpisodes(seriesId: String, seasonId: String) {
        loadEpisodesJob?.cancel()

        // Check cache first
        if (episodesCache.containsKey(seasonId)) {
            _state.value = _state.value.copy(
                episodes = episodesCache[seasonId] ?: emptyList(),
                isLoadingEpisodes = false
            )
            return
        }

        loadEpisodesJob = viewModelScope.launch(dispatchers.io) {
            _state.value = _state.value.copy(isLoadingEpisodes = true, episodes = emptyList())
            val result = getEpisodesUseCase(seriesId, seasonId)

            result.onSuccess { episodes ->
                episodesCache[seasonId] = episodes
                _state.value = _state.value.copy(
                    episodes = episodes,
                    isLoadingEpisodes = false
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoadingEpisodes = false, error = e.message)
            }
        }
    }

    fun updateScrollPosition(position: Int) {
        _state.value = _state.value.copy(scrollPosition = position)
    }
}
