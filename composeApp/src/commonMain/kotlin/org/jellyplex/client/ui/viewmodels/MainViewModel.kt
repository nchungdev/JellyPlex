package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    private val getTvShowsUseCase: GetTvShowsUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val getMoviesCacheUseCase: GetMoviesCacheUseCase,
    private val saveMoviesCacheUseCase: SaveMoviesCacheUseCase,
    private val getTvShowsCacheUseCase: GetTvShowsCacheUseCase,
    private val saveTvShowsCacheUseCase: SaveTvShowsCacheUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val url = getBaseUrlUseCase()
        if (!hasSessionUseCase() || url.isEmpty()) {
            println("MainViewModel: No valid session or empty URL, skipping data load.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            println("MainViewModel: Loading data for server: $url")
            _state.value = _state.value.copy(baseUrl = url)

            // 1. Load from cache first
            println("MainViewModel: Checking local cache...")
            val cachedMovies = getMoviesCacheUseCase()
            val cachedTvShows = getTvShowsCacheUseCase()

            if (cachedMovies != null || cachedTvShows != null) {
                println("MainViewModel: Cache found. Items - Movies: ${cachedMovies?.size ?: 0}, TV: ${cachedTvShows?.size ?: 0}")
                val movies = cachedMovies ?: emptyList()
                val series = cachedTvShows ?: emptyList()
                _state.value = _state.value.copy(
                    items = movies + series,
                    movies = movies,
                    tvShows = series
                )
            } else {
                println("MainViewModel: No local cache available.")
            }

            // 2. Fetch from API
            println("MainViewModel: Fetching fresh data from API...")
            _state.value = _state.value.copy(isLoading = true)

            val moviesDeferred = async { getMoviesUseCase() }
            val tvShowsDeferred = async { getTvShowsUseCase() }

            val moviesResult = moviesDeferred.await()
            val tvShowsResult = tvShowsDeferred.await()

            if (moviesResult.isSuccess && tvShowsResult.isSuccess) {
                val movies = moviesResult.getOrDefault(emptyList())
                val series = tvShowsResult.getOrDefault(emptyList())
                println("MainViewModel: API fetch SUCCESS. Movies: ${movies.size}, TV: ${series.size}")
                
                _state.value = _state.value.copy(
                    items = movies + series,
                    movies = movies,
                    tvShows = series,
                    isLoading = false,
                    error = null
                )
                // Update cache
                saveMoviesCacheUseCase(movies)
                saveTvShowsCacheUseCase(series)
            } else {
                val errorResult = moviesResult.exceptionOrNull() ?: tvShowsResult.exceptionOrNull()
                val error = errorResult?.message
                println("MainViewModel: API fetch FAILED. Error: $error")
                
                _state.value = _state.value.copy(isLoading = false, error = error)

                if (error?.contains("401") == true || error?.contains("Unauthorized") == true) {
                    println("MainViewModel: Authentication error (401). Clearing session.")
                    clearSessionUseCase()
                }
            }
        }
    }
}
