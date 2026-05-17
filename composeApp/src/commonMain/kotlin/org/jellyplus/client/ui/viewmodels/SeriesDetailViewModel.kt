package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.usecases.GetBaseUrlUseCase
import org.jellyplus.client.domain.usecases.GetEpisodesUseCase
import org.jellyplus.client.domain.usecases.GetHomeContentUseCase
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
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(SeriesDetailState())
    val state: StateFlow<SeriesDetailState> = _state.asStateFlow()

    private val episodesCache = mutableMapOf<String, List<MediaItem>>()

    private var loadSeriesJob: Job? = null
    private var debouncedLoadEpisodesJob: Job? = null
    private val loadEpisodeJobs = mutableMapOf<String, Job>()

    fun loadSeriesDetails(seriesId: String, focusSeasonId: String? = null) {
        // Luôn cập nhật baseUrl mới nhất từ session
        val currentBaseUrl = getBaseUrlUseCase()
        if (_state.value.baseUrl != currentBaseUrl) {
            _state.value = _state.value.copy(baseUrl = currentBaseUrl)
        }

        if (_state.value.seriesId == seriesId && _state.value.seasons.isNotEmpty()) {
            // Series already loaded — focus the explicit season, otherwise the
            // season of the most recently played episode (e.g. back navigation).
            val targetId = focusSeasonId ?: resumeSeasonIdFor(seriesId, _state.value.seasons)
            if (targetId != null) {
                val target = _state.value.seasons.find { it.id == targetId }
                if (target != null && target.id != _state.value.selectedSeason?.id) {
                    selectSeason(target, debounce = false)
                }
            }
            return
        }

        loadSeriesJob?.cancel()
        loadSeriesJob = viewModelScope.launch(dispatchers.io) {
            val previousSeriesId = _state.value.seriesId
            if (previousSeriesId != seriesId) {
                debouncedLoadEpisodesJob?.cancel()
                loadEpisodeJobs.values.forEach { it.cancel() }
                loadEpisodeJobs.clear()
                episodesCache.clear()
            }

            _state.value = _state.value.copy(
                seriesId = seriesId,
                isLoadingSeasons = true
            )
            val result = getSeasonsUseCase(seriesId)

            result.onSuccess { seasons ->
                val lastSelectedSeasonId = _state.value.selectedSeason?.id
                val resumeSeasonId = resumeSeasonIdFor(seriesId, seasons)
                // Priority: explicit focus → season of last played episode →
                // last selected → first unwatched → first.
                val seasonToSelect = seasons.find { it.id == focusSeasonId }
                    ?: seasons.find { it.id == resumeSeasonId }
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

    /**
     * The season id containing the most recently played (resume) episode for
     * this series, derived from the shared home/resume cache. Falls back to
     * matching the episode's season number when the season id is absent.
     */
    private fun resumeSeasonIdFor(seriesId: String, seasons: List<MediaItem>): String? {
        val resumeEpisode = getHomeContentUseCase().value?.resumeItems
            ?.firstOrNull { it.seriesId == seriesId } ?: return null
        return resumeEpisode.seasonId
            ?: seasons.firstOrNull { it.index != null && it.index == resumeEpisode.parentIndexNumber }?.id
    }

    fun selectTabIndex(index: Int) {
        _state.value = _state.value.copy(selectedTabIndex = index)
    }

    fun selectSeason(season: MediaItem, debounce: Boolean = true) {
        if (_state.value.selectedSeason?.id == season.id) return

        val seriesId = _state.value.seriesId ?: return
        val cachedEpisodes = episodesCache[season.id]
        _state.value = _state.value.copy(
            selectedSeason = season,
            episodes = cachedEpisodes ?: emptyList(),
            isLoadingEpisodes = cachedEpisodes == null,
        )
        if (cachedEpisodes != null) {
            debouncedLoadEpisodesJob?.cancel()
            return
        }

        debouncedLoadEpisodesJob?.cancel()
        if (!debounce) {
            loadEpisodes(seriesId, season.id)
            return
        }

        debouncedLoadEpisodesJob = viewModelScope.launch {
            delay(SEASON_EPISODE_LOAD_DEBOUNCE_MS)
            if (_state.value.seriesId == seriesId && _state.value.selectedSeason?.id == season.id) {
                loadEpisodes(seriesId, season.id)
            }
        }
    }

    private fun loadEpisodes(seriesId: String, seasonId: String) {
        episodesCache[seasonId]?.let { episodes ->
            _state.value = _state.value.copy(
                episodes = if (_state.value.selectedSeason?.id == seasonId) episodes else _state.value.episodes,
                isLoadingEpisodes = if (_state.value.selectedSeason?.id == seasonId) false else _state.value.isLoadingEpisodes,
            )
            return
        }

        if (loadEpisodeJobs[seasonId]?.isActive == true) return

        loadEpisodeJobs[seasonId] = viewModelScope.launch(dispatchers.io) {
            if (_state.value.selectedSeason?.id == seasonId) {
                _state.value = _state.value.copy(isLoadingEpisodes = true, episodes = emptyList())
            }

            val result = getEpisodesUseCase(seriesId, seasonId)

            result.onSuccess { episodes ->
                episodesCache[seasonId] = episodes
                loadEpisodeJobs.remove(seasonId)
                if (_state.value.seriesId == seriesId && _state.value.selectedSeason?.id == seasonId) {
                    _state.value = _state.value.copy(
                        episodes = episodes,
                        isLoadingEpisodes = false
                    )
                }
            }.onFailure { e ->
                loadEpisodeJobs.remove(seasonId)
                if (_state.value.seriesId == seriesId && _state.value.selectedSeason?.id == seasonId) {
                    _state.value = _state.value.copy(isLoadingEpisodes = false, error = e.message)
                }
            }
        }
    }

    fun updateScrollPosition(position: Int) {
        _state.value = _state.value.copy(scrollPosition = position)
    }

    private companion object {
        const val SEASON_EPISODE_LOAD_DEBOUNCE_MS = 250L
    }
}
