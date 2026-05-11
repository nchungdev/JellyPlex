package org.jellyplex.client.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.data.datasource.local.ISessionLocalDataSource

class SessionManager(private val settings: Settings = Settings()) : ISessionLocalDataSource {
    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_USER_ID = "user_id"
    }

    private val _isAuthenticated = MutableStateFlow(hasSession())
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    override var baseUrl: String?
        get() = settings.getStringOrNull(KEY_BASE_URL)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_BASE_URL, value) else settings.remove(KEY_BASE_URL)
            updateAuthState()
        }

    override var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_ACCESS_TOKEN, value) else settings.remove(KEY_ACCESS_TOKEN)
            updateAuthState()
        }

    override var userName: String?
        get() = settings.getStringOrNull(KEY_USER_NAME)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_USER_NAME, value) else settings.remove(KEY_USER_NAME)
        }

    override var password: String?
        get() = settings.getStringOrNull(KEY_PASSWORD)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_PASSWORD, value) else settings.remove(KEY_PASSWORD)
        }

    override var userId: String?
        get() = settings.getStringOrNull(KEY_USER_ID)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_USER_ID, value) else settings.remove(KEY_USER_ID)
        }

    override fun clear() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_PASSWORD)
        settings.remove(KEY_BASE_URL)
        updateAuthState()
    }

    private fun updateAuthState() {
        _isAuthenticated.value = hasSession()
    }

    override fun hasSession(): Boolean {
        val token = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        val url = settings.getStringOrNull(KEY_BASE_URL)
        return !token.isNullOrEmpty() && !url.isNullOrEmpty()
    }
}
