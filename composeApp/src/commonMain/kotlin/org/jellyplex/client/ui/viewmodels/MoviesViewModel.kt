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

data class MoviesState(
    val movies: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class MoviesViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val getMoviesCacheUseCase: GetMoviesCacheUseCase,
    private val saveMoviesCacheUseCase: SaveMoviesCacheUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MoviesState())
    val state: StateFlow<MoviesState> = _state.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        if (!hasSessionUseCase()) return
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Try cache first
            val cached = getMoviesCacheUseCase()
            if (!cached.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    movies = cached,
                    baseUrl = getBaseUrlUseCase()
                )
            }

            // 2. Fetch from API
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = getMoviesUseCase()

            result.onSuccess { items ->
                _state.value = _state.value.copy(
                    movies = items,
                    baseUrl = getBaseUrlUseCase(),
                    isLoading = false
                )
                saveMoviesCacheUseCase(items)
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
