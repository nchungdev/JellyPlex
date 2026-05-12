package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.usecases.*

data class TvShowsState(
    val tvShows: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TvShowsViewModel(
    private val getTvShowsUseCase: GetTvShowsUseCase,
    private val refreshTvShowsUseCase: RefreshTvShowsUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(TvShowsState())
    val state: StateFlow<TvShowsState> = _state.asStateFlow()

    init {
        // SSOT: Observe local data flow
        viewModelScope.launch {
            getTvShowsUseCase().collect { tvShows ->
                if (tvShows != null) {
                    _state.value = _state.value.copy(
                        tvShows = tvShows,
                        baseUrl = getBaseUrlUseCase()
                    )
                }
            }
        }

        loadTvShows()
    }

    fun loadTvShows() {
        if (!hasSessionUseCase()) return
        
        // Launch on Main thread
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Repo handles the threading
            val result = refreshTvShowsUseCase()

            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
