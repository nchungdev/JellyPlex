package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
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
    val watchLaterIds: Set<String> = emptySet(),
    val watchLaterItems: List<MediaItem> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val favoriteItems: List<MediaItem> = emptyList(),
    val toggledFavoriteIds: Set<String> = emptySet(),
    val error: String? = null
)

class MainViewModel(
    private val getMoviesPageUseCase: GetMoviesPageUseCase,
    private val getTvShowsPageUseCase: GetTvShowsPageUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getWatchLaterIdsUseCase: GetWatchLaterIdsUseCase,
    private val refreshWatchLaterIdsUseCase: RefreshWatchLaterIdsUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
    private val setWatchLaterUseCase: SetWatchLaterUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()
    private val pageSize = 10

    // Registry of every MediaItem the VM has seen (loaded lists, hero/featured
    // items fed by screens, toggled items) so Favorites / Watch Later can
    // resolve their ids to items even when the item isn't in the loaded page.
    private val itemRegistry = LinkedHashMap<String, MediaItem>()

    private fun register(items: List<MediaItem>) {
        items.forEach { itemRegistry[it.id] = it }
    }

    /** Screens feed hero/featured/resume/recent items here so toggling
     *  favorite/watch-later on them is reflected in the dedicated screens. */
    fun registerItems(items: List<MediaItem>) {
        if (items.isEmpty()) return
        register(items)
        recomputeLists()
    }

    private fun recomputeLists() {
        val s = _state.value
        _state.value = s.copy(
            favoriteItems = s.favoriteIds.mapNotNull { itemRegistry[it] },
            watchLaterItems = s.watchLaterIds.mapNotNull { itemRegistry[it] },
        )
    }

    init {
        observeWatchLater()
        loadData()
    }

    private fun observeWatchLater() {
        viewModelScope.launch(dispatchers.io) {
            getWatchLaterIdsUseCase().collectLatest { ids ->
                _state.value = _state.value.copy(
                    watchLaterIds = ids,
                    watchLaterItems = ids.mapNotNull { itemRegistry[it] },
                )
            }
        }
    }

    fun loadData() {
        if (!hasSessionUseCase()) return
        if (_state.value.isLoading) {
            logDebug("JellyPerf", "VM Main loadData skipped: already loading")
            return
        }

        viewModelScope.launch(dispatchers.io) {
            val mark = TimeSource.Monotonic.markNow()
            logDebug("JellyPerf", "VM Main loadData -> start pageSize=$pageSize")
            _state.value = _state.value.copy(isLoading = true, hasLoaded = false, error = null)
            refreshWatchLaterIdsUseCase()

            val moviesDeferred = async { getMoviesPageUseCase(startIndex = 0, limit = pageSize) }
            val tvShowsDeferred = async { getTvShowsPageUseCase(startIndex = 0, limit = pageSize) }

            val moviesResult = moviesDeferred.await()
            val tvShowsResult = tvShowsDeferred.await()

            if (moviesResult.isSuccess && tvShowsResult.isSuccess) {
                val moviesPage = moviesResult.getOrThrow()
                val tvShowsPage = tvShowsResult.getOrThrow()
                val loaded = moviesPage.items + tvShowsPage.items
                register(loaded)
                _state.value = _state.value.copy(
                    items = loaded,
                    movies = moviesPage.items,
                    tvShows = tvShowsPage.items,
                    favoriteIds = seedFavorites(loaded),
                    baseUrl = getBaseUrlUseCase(),
                    isLoading = false,
                    hasLoaded = true,
                    hasMoreMovies = moviesPage.items.size < moviesPage.totalRecordCount,
                    hasMoreTvShows = tvShowsPage.items.size < tvShowsPage.totalRecordCount,
                )
                recomputeLists()
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
                register(page.items)
                _state.value = _state.value.copy(
                    movies = nextMovies,
                    items = nextMovies + _state.value.tvShows,
                    favoriteIds = seedFavorites(nextMovies + _state.value.tvShows),
                    isLoadingMoreMovies = false,
                    hasMoreMovies = nextMovies.size < page.totalRecordCount,
                )
                recomputeLists()
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
                register(page.items)
                _state.value = _state.value.copy(
                    tvShows = nextShows,
                    items = _state.value.movies + nextShows,
                    favoriteIds = seedFavorites(_state.value.movies + nextShows),
                    isLoadingMoreTvShows = false,
                    hasMoreTvShows = nextShows.size < page.totalRecordCount,
                )
                recomputeLists()
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

    private fun seedFavorites(items: List<MediaItem>): Set<String> {
        val s = _state.value
        return s.favoriteIds +
            items.filter { it.userData?.isFavorite == true && it.id !in s.toggledFavoriteIds }.map { it.id }
    }

    fun isFavorite(item: MediaItem): Boolean {
        val s = _state.value
        if (item.id in s.favoriteIds) return true
        // Once the user has toggled an item this session, favoriteIds is the
        // single source of truth (don't trust the now-stale passed userData).
        if (item.id in s.toggledFavoriteIds) return false
        return findItem(item.id)?.userData?.isFavorite ?: item.userData?.isFavorite ?: false
    }

    fun isWatchLater(item: MediaItem): Boolean = item.id in _state.value.watchLaterIds

    fun toggleFavorite(item: MediaItem) {
        val userId = getUserIdUseCase() ?: return
        register(listOf(item))
        val next = !isFavorite(item)
        updateFavoriteState(item.id, next)
        viewModelScope.launch(dispatchers.io) {
            runCatching { setFavoriteUseCase(userId, item.id, next) }
                .onFailure { updateFavoriteState(item.id, !next) }
        }
    }

    fun toggleWatchLater(item: MediaItem) {
        register(listOf(item))
        val next = !isWatchLater(item)
        setWatchLaterUseCase(item.id, next)
        _state.value = _state.value.copy(
            watchLaterItems = if (next) {
                (_state.value.watchLaterItems + item).distinctBy { it.id }
            } else {
                _state.value.watchLaterItems.filterNot { it.id == item.id }
            }
        )
    }

    private fun findItem(itemId: String): MediaItem? =
        _state.value.items.firstOrNull { it.id == itemId } ?: itemRegistry[itemId]

    private fun updateFavoriteState(itemId: String, favorite: Boolean) {
        fun List<MediaItem>.updated() = map { if (it.id == itemId) it.withFavorite(favorite) else it }
        val movies = _state.value.movies.updated()
        val tvShows = _state.value.tvShows.updated()
        val items = movies + tvShows
        _state.value = _state.value.copy(
            movies = movies,
            tvShows = tvShows,
            items = items,
            watchLaterItems = _state.value.watchLaterItems.updated(),
            favoriteIds = if (favorite) {
                _state.value.favoriteIds + itemId
            } else {
                _state.value.favoriteIds - itemId
            },
            toggledFavoriteIds = _state.value.toggledFavoriteIds + itemId,
        )
        recomputeLists()
    }
}
