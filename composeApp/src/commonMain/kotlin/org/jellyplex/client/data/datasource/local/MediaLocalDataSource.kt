package org.jellyplex.client.data.datasource.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jellyplex.client.data.local.CacheManager
import org.jellyplex.client.domain.models.HomeContent
import org.jellyplex.client.domain.models.MediaItem

interface IMediaLocalDataSource {
    val homeContent: StateFlow<HomeContent?>
    val movies: StateFlow<List<MediaItem>?>
    val tvShows: StateFlow<List<MediaItem>?>

    suspend fun saveHomeCache(content: HomeContent)
    suspend fun saveMoviesCache(items: List<MediaItem>)
    suspend fun saveTvShowsCache(items: List<MediaItem>)
    fun clear()
}


class MediaLocalDataSource(
    private val cacheManager: CacheManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : IMediaLocalDataSource {
    private val _homeContent = MutableStateFlow<HomeContent?>(null)

    override val homeContent: StateFlow<HomeContent?> = _homeContent.asStateFlow()
    private val _movies = MutableStateFlow<List<MediaItem>?>(null)

    override val movies: StateFlow<List<MediaItem>?> = _movies.asStateFlow()
    private val _tvShows = MutableStateFlow<List<MediaItem>?>(null)

    override val tvShows: StateFlow<List<MediaItem>?> = _tvShows.asStateFlow()

    init {
        // Initial load from persistent storage
        loadFromCache()
    }

    private fun loadFromCache() {
        _homeContent.value = cacheManager.homeCache?.let {
            try {
                json.decodeFromString(HomeContent.serializer(), it)
            } catch (e: Exception) {
                null
            }
        }
        _movies.value = cacheManager.moviesCache?.let {
            try {
                json.decodeFromString(ListSerializer(MediaItem.serializer()), it)
            } catch (e: Exception) {
                null
            }
        }
        _tvShows.value = cacheManager.tvShowsCache?.let {
            try {
                json.decodeFromString(ListSerializer(MediaItem.serializer()), it)
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun saveHomeCache(content: HomeContent) {
        cacheManager.homeCache = json.encodeToString(HomeContent.serializer(), content)
        _homeContent.value = content
    }

    override suspend fun saveMoviesCache(items: List<MediaItem>) {
        cacheManager.moviesCache = json.encodeToString(ListSerializer(MediaItem.serializer()), items)
        _movies.value = items
    }

    override suspend fun saveTvShowsCache(items: List<MediaItem>) {
        cacheManager.tvShowsCache = json.encodeToString(ListSerializer(MediaItem.serializer()), items)
        _tvShows.value = items
    }

    override fun clear() {
        cacheManager.clear()
        _homeContent.value = null
        _movies.value = null
        _tvShows.value = null
    }
}

class InMemorySessionLocalDataSource : ISessionLocalDataSource {
    override var baseUrl: String? = null
    override var accessToken: String? = null
    override var userName: String? = null
    override var password: String? = null

    override var userId: String? = null
    private val _isAuthenticated = MutableStateFlow(false)

    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    override fun clear() {
        baseUrl = null
        accessToken = null
        userName = null
        password = null
        userId = null
        _isAuthenticated.value = false
    }

    override fun hasSession(): Boolean {
        return !accessToken.isNullOrEmpty() && !baseUrl.isNullOrEmpty()
    }

    fun updateAuthState() {
        _isAuthenticated.value = hasSession()
    }
}
