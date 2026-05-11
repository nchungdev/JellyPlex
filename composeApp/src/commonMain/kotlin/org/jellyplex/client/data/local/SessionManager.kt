package org.jellyplex.client.data.local

import com.russhwolf.settings.Settings

class SessionManager(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_CACHE_HOME = "cache_home"
        private const val KEY_CACHE_MOVIES = "cache_movies"
        private const val KEY_CACHE_TVSHOWS = "cache_tvshows"
    }

    private val _isAuthenticated = kotlinx.coroutines.flow.MutableStateFlow(hasSession())
    val isAuthenticated: kotlinx.coroutines.flow.StateFlow<Boolean> = _isAuthenticated

    var baseUrl: String?
        get() = settings.getStringOrNull(KEY_BASE_URL)
        set(value) {
            if (!value.isNullOrEmpty()) {
                settings.putString(KEY_BASE_URL, value)
            } else {
                settings.remove(KEY_BASE_URL)
            }
            updateAuthState()
        }

    var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (!value.isNullOrEmpty()) {
                settings.putString(KEY_ACCESS_TOKEN, value)
            } else {
                settings.remove(KEY_ACCESS_TOKEN)
            }
            updateAuthState()
        }

    var userName: String?
        get() = settings.getStringOrNull(KEY_USER_NAME)
        set(value) {
            if (!value.isNullOrEmpty()) {
                settings.putString(KEY_USER_NAME, value)
            } else {
                settings.remove(KEY_USER_NAME)
            }
        }

    var password: String?
        get() = settings.getStringOrNull(KEY_PASSWORD)
        set(value) {
            if (!value.isNullOrEmpty()) {
                settings.putString(KEY_PASSWORD, value)
            } else {
                settings.remove(KEY_PASSWORD)
            }
        }

    var userId: String?
        get() = settings.getStringOrNull(KEY_USER_ID)
        set(value) {
            if (!value.isNullOrEmpty()) {
                settings.putString(KEY_USER_ID, value)
            } else {
                settings.remove(KEY_USER_ID)
            }
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
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_PASSWORD)
        settings.remove(KEY_BASE_URL)
        settings.remove(KEY_CACHE_HOME)
        settings.remove(KEY_CACHE_MOVIES)
        settings.remove(KEY_CACHE_TVSHOWS)
        updateAuthState()
    }

    private fun updateAuthState() {
        _isAuthenticated.value = hasSession()
    }

    fun hasSession(): Boolean {
        val token = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        val url = settings.getStringOrNull(KEY_BASE_URL)
        return !token.isNullOrEmpty() && !url.isNullOrEmpty()
    }
}
