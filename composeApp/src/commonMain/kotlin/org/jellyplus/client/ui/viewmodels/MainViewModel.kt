package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.usecases.*
import org.jellyplus.client.logDebug
import org.jellyplus.client.logError
import kotlin.time.TimeSource

data class MainState(
    val items: List<MediaItem> = emptyList(),
    val movies: List<MediaItem> = emptyList(),
    val tvShows: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val isLoadingMoreMovies: Boolean = false,
    val isLoadingMoreTvShows: Boolean = false,
    val hasMoreMovies: Boolean = true,
    val hasMoreTvShows: Boolean = true,
    val error: String? = null
)

class MainViewModel(
    private val getMoviesPageUseCase: GetMoviesPageUseCase,
    private val getTvShowsPageUseCase: GetTvShowsPageUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()
    private val pageSize = 10

    init {
        loadData()
    }

    fun loadData() {
        if (!hasSessionUseCase()) return

        viewModelScope.launch(dispatchers.io) {
            val mark = TimeSource.Monotonic.markNow()
            logDebug("JellyPerf", "VM Main loadData -> start pageSize=$pageSize")
            _state.value = _state.value.copy(isLoading = true, hasLoaded = false, error = null)

            val moviesDeferred = async { getMoviesPageUseCase(startIndex = 0, limit = pageSize) }
            val tvShowsDeferred = async { getTvShowsPageUseCase(startIndex = 0, limit = pageSize) }

            val moviesResult = moviesDeferred.await()
            val tvShowsResult = tvShowsDeferred.await()

            if (moviesResult.isSuccess && tvShowsResult.isSuccess) {
                val moviesPage = moviesResult.getOrThrow()
                val tvShowsPage = tvShowsResult.getOrThrow()
                _state.value = _state.value.copy(
                    items = moviesPage.items + tvShowsPage.items,
                    movies = moviesPage.items,
                    tvShows = tvShowsPage.items,
                    baseUrl = getBaseUrlUseCase(),
                    isLoading = false,
                    hasLoaded = true,
                    hasMoreMovies = moviesPage.items.size < moviesPage.totalRecordCount,
                    hasMoreTvShows = tvShowsPage.items.size < tvShowsPage.totalRecordCount,
                )
                logDebug(
                    "JellyPerf",
                    "VM Main loadData <- success ${mark.elapsedNow().inWholeMilliseconds}ms " +
                        "movies=${moviesPage.items.size}/${moviesPage.totalRecordCount} tv=${tvShowsPage.items.size}/${tvShowsPage.totalRecordCount}"
                )
            } else {
                val errorResult = moviesResult.exceptionOrNull() ?: tvShowsResult.exceptionOrNull()
                val error = errorResult?.message
                _state.value = _state.value.copy(isLoading = false, hasLoaded = true, error = error)
                logError("JellyPerf", "VM Main loadData <- failed ${mark.elapsedNow().inWholeMilliseconds}ms: $error", errorResult)

                if (error?.contains("401") == true || error?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }

    fun loadMoreMovies() {
        val current = _state.value
        if (!hasSessionUseCase() || current.isLoadingMoreMovies || !current.hasMoreMovies) return

        viewModelScope.launch(dispatchers.io) {
            val mark = TimeSource.Monotonic.markNow()
            val startIndex = _state.value.movies.size
            logDebug("JellyPerf", "VM Main loadMoreMovies -> start start=$startIndex limit=$pageSize")
            _state.value = _state.value.copy(isLoadingMoreMovies = true, error = null)
            val result = getMoviesPageUseCase(startIndex = startIndex, limit = pageSize)
            result.onSuccess { page ->
                val nextMovies = (_state.value.movies + page.items).distinctBy { it.id }
                _state.value = _state.value.copy(
                    movies = nextMovies,
                    items = nextMovies + _state.value.tvShows,
                    isLoadingMoreMovies = false,
                    hasMoreMovies = nextMovies.size < page.totalRecordCount,
                )
                logDebug("JellyPerf", "VM Main loadMoreMovies <- success ${mark.elapsedNow().inWholeMilliseconds}ms loaded=${nextMovies.size}/${page.totalRecordCount}")
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoadingMoreMovies = false, error = e.message)
                logError("JellyPerf", "VM Main loadMoreMovies <- failed ${mark.elapsedNow().inWholeMilliseconds}ms: ${e.message}", e)
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }

    fun loadMoreTvShows() {
        val current = _state.value
        if (!hasSessionUseCase() || current.isLoadingMoreTvShows || !current.hasMoreTvShows) return

        viewModelScope.launch(dispatchers.io) {
            val mark = TimeSource.Monotonic.markNow()
            val startIndex = _state.value.tvShows.size
            logDebug("JellyPerf", "VM Main loadMoreTvShows -> start start=$startIndex limit=$pageSize")
            _state.value = _state.value.copy(isLoadingMoreTvShows = true, error = null)
            val result = getTvShowsPageUseCase(startIndex = startIndex, limit = pageSize)
            result.onSuccess { page ->
                val nextShows = (_state.value.tvShows + page.items).distinctBy { it.id }
                _state.value = _state.value.copy(
                    tvShows = nextShows,
                    items = _state.value.movies + nextShows,
                    isLoadingMoreTvShows = false,
                    hasMoreTvShows = nextShows.size < page.totalRecordCount,
                )
                logDebug("JellyPerf", "VM Main loadMoreTvShows <- success ${mark.elapsedNow().inWholeMilliseconds}ms loaded=${nextShows.size}/${page.totalRecordCount}")
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoadingMoreTvShows = false, error = e.message)
                logError("JellyPerf", "VM Main loadMoreTvShows <- failed ${mark.elapsedNow().inWholeMilliseconds}ms: ${e.message}", e)
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
