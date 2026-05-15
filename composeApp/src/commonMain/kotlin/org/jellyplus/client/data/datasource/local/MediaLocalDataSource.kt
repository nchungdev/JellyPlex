package org.jellyplus.client.data.datasource.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jellyplus.client.domain.models.HomeContent
import org.jellyplus.client.domain.models.MediaItem

class MediaLocalDataSource(
    private val settings: Settings = Settings(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : IMediaLocalDataSource {
    companion object {
        private const val KEY_CACHE_HOME = "cache_home"
        private const val KEY_CACHE_MOVIES = "cache_movies"
        private const val KEY_CACHE_TVSHOWS = "cache_tvshows"
        private const val KEY_WATCH_LATER_IDS = "watch_later_ids"
    }

    private val _homeContent = MutableStateFlow<HomeContent?>(null)
    override val homeContent: StateFlow<HomeContent?> = _homeContent.asStateFlow()

    private val _movies = MutableStateFlow<List<MediaItem>?>(null)
    override val movies: StateFlow<List<MediaItem>?> = _movies.asStateFlow()

    private val _tvShows = MutableStateFlow<List<MediaItem>?>(null)
    override val tvShows: StateFlow<List<MediaItem>?> = _tvShows.asStateFlow()

    private val _watchLaterIds = MutableStateFlow(loadWatchLaterIds())
    val watchLaterIds: StateFlow<Set<String>> = _watchLaterIds.asStateFlow()

    init {
        loadFromCache()
    }

    private fun loadFromCache() {
        _homeContent.value = settings.getStringOrNull(KEY_CACHE_HOME)?.let {
            try { json.decodeFromString(HomeContent.serializer(), it) } catch (e: Exception) { null }
        }
        _movies.value = settings.getStringOrNull(KEY_CACHE_MOVIES)?.let {
            try { json.decodeFromString(ListSerializer(MediaItem.serializer()), it) } catch (e: Exception) { null }
        }
        _tvShows.value = settings.getStringOrNull(KEY_CACHE_TVSHOWS)?.let {
            try { json.decodeFromString(ListSerializer(MediaItem.serializer()), it) } catch (e: Exception) { null }
        }
    }

    override suspend fun saveHomeCache(content: HomeContent) {
        settings.putString(KEY_CACHE_HOME, json.encodeToString(HomeContent.serializer(), content))
        _homeContent.value = content
    }

    override suspend fun saveMoviesCache(items: List<MediaItem>) {
        settings.putString(KEY_CACHE_MOVIES, json.encodeToString(ListSerializer(MediaItem.serializer()), items))
        _movies.value = items
    }

    override suspend fun saveTvShowsCache(items: List<MediaItem>) {
        settings.putString(KEY_CACHE_TVSHOWS, json.encodeToString(ListSerializer(MediaItem.serializer()), items))
        _tvShows.value = items
    }

    override fun clear() {
        settings.remove(KEY_CACHE_HOME)
        settings.remove(KEY_CACHE_MOVIES)
        settings.remove(KEY_CACHE_TVSHOWS)
        _homeContent.value = null
        _movies.value = null
        _tvShows.value = null
    }

    fun setWatchLater(itemId: String, enabled: Boolean) {
        val next = if (enabled) _watchLaterIds.value + itemId else _watchLaterIds.value - itemId
        settings.putString(KEY_WATCH_LATER_IDS, json.encodeToString(SetSerializer(String.serializer()), next))
        _watchLaterIds.value = next
    }

    private fun loadWatchLaterIds(): Set<String> {
        return settings.getStringOrNull(KEY_WATCH_LATER_IDS)?.let {
            runCatching { json.decodeFromString(SetSerializer(String.serializer()), it) }.getOrNull()
        } ?: emptySet()
    }
}
