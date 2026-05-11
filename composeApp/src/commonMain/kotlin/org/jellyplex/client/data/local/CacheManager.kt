package org.jellyplex.client.data.local

import com.russhwolf.settings.Settings

class CacheManager(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_CACHE_HOME = "cache_home"
        private const val KEY_CACHE_MOVIES = "cache_movies"
        private const val KEY_CACHE_TVSHOWS = "cache_tvshows"
    }

    var homeCache: String?
        get() = settings.getStringOrNull(KEY_CACHE_HOME)
        set(value) = if (value == null) settings.remove(KEY_CACHE_HOME) else settings.putString(KEY_CACHE_HOME, value)

    var moviesCache: String?
        get() = settings.getStringOrNull(KEY_CACHE_MOVIES)
        set(value) = if (value == null) settings.remove(KEY_CACHE_MOVIES) else settings.putString(KEY_CACHE_MOVIES, value)

    var tvShowsCache: String?
        get() = settings.getStringOrNull(KEY_CACHE_TVSHOWS)
        set(value) = if (value == null) settings.remove(KEY_CACHE_TVSHOWS) else settings.putString(KEY_CACHE_TVSHOWS, value)

    fun clear() {
        settings.remove(KEY_CACHE_HOME)
        settings.remove(KEY_CACHE_MOVIES)
        settings.remove(KEY_CACHE_TVSHOWS)
    }
}
