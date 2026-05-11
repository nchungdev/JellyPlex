package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jellyplex.client.domain.models.AppDispatchers
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.usecases.*

data class MainState(
    val items: List<MediaItem> = emptyList(),
    val movies: List<MediaItem> = emptyList(),
    val tvShows: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class MainViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val refreshMoviesUseCase: RefreshMoviesUseCase,
    private val getTvShowsUseCase: GetTvShowsUseCase,
    private val refreshTvShowsUseCase: RefreshTvShowsUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        // SSOT: Observe local data flows
        viewModelScope.launch {
            combine(getMoviesUseCase(), getTvShowsUseCase()) { movies, tvShows ->
                if (movies != null || tvShows != null) {
                    val m = movies ?: emptyList()
                    val t = tvShows ?: emptyList()
                    _state.value = _state.value.copy(
                        items = m + t,
                        movies = m,
                        tvShows = t,
                        baseUrl = getBaseUrlUseCase()
                    )
                }
            }.collect {}
        }

        loadData()
    }

    fun loadData() {
        if (!hasSessionUseCase()) return

        viewModelScope.launch(dispatchers.io) {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val moviesDeferred = async { refreshMoviesUseCase() }
            val tvShowsDeferred = async { refreshTvShowsUseCase() }

            val moviesResult = moviesDeferred.await()
            val tvShowsResult = tvShowsDeferred.await()

            if (moviesResult.isSuccess && tvShowsResult.isSuccess) {
                _state.value = _state.value.copy(isLoading = false)
            } else {
                val errorResult = moviesResult.exceptionOrNull() ?: tvShowsResult.exceptionOrNull()
                val error = errorResult?.message
                _state.value = _state.value.copy(isLoading = false, error = error)

                if (error?.contains("401") == true || error?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
