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

data class MoviesState(
    val movies: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class MoviesViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val refreshMoviesUseCase: RefreshMoviesUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(MoviesState())
    val state: StateFlow<MoviesState> = _state.asStateFlow()

    init {
        // SSOT: Observe local data flow
        viewModelScope.launch {
            getMoviesUseCase().collect { movies ->
                if (movies != null) {
                    _state.value = _state.value.copy(
                        movies = movies,
                        baseUrl = getBaseUrlUseCase()
                    )
                }
            }
        }

        loadMovies()
    }

    fun loadMovies() {
        if (!hasSessionUseCase()) return
        
        // Launch on Main thread
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Repo handles the threading
            val result = refreshMoviesUseCase()

            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false)
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
