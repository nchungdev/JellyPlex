package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.usecases.*

data class TvShowsState(
    val tvShows: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TvShowsViewModel(
    private val getTvShowsUseCase: GetTvShowsUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val getTvShowsCacheUseCase: GetTvShowsCacheUseCase,
    private val saveTvShowsCacheUseCase: SaveTvShowsCacheUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(TvShowsState())
    val state: StateFlow<TvShowsState> = _state.asStateFlow()

    init {
        loadTvShows()
    }

    fun loadTvShows() {
        if (!hasSessionUseCase()) return
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Try cache first
            val cached = getTvShowsCacheUseCase()
            if (!cached.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    tvShows = cached,
                    baseUrl = getBaseUrlUseCase()
                )
            }

            // 2. Fetch from API
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = getTvShowsUseCase()

            result.onSuccess { tvShows ->
                _state.value = _state.value.copy(
                    tvShows = tvShows,
                    baseUrl = getBaseUrlUseCase(),
                    isLoading = false,
                )
                saveTvShowsCacheUseCase(tvShows)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
