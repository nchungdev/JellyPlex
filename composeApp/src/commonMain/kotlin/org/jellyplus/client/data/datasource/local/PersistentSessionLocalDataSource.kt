package org.jellyplus.client.data.datasource.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jellyplus.client.getDeviceName
import org.jellyplus.client.utils.generateUuid

class PersistentSessionLocalDataSource(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_DEVICE_NAME = "device_name"
    }

    private val _isAuthenticated = MutableStateFlow(hasSession())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    var baseUrl: String?
        get() = settings.getStringOrNull(KEY_BASE_URL)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_BASE_URL, value) else settings.remove(KEY_BASE_URL)
            updateAuthState()
        }

    var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_ACCESS_TOKEN, value) else settings.remove(KEY_ACCESS_TOKEN)
            updateAuthState()
        }

    var userName: String?
        get() = settings.getStringOrNull(KEY_USER_NAME)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_USER_NAME, value) else settings.remove(KEY_USER_NAME)
        }

    var password: String?
        get() = settings.getStringOrNull(KEY_PASSWORD)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_PASSWORD, value) else settings.remove(KEY_PASSWORD)
        }

    var userId: String?
        get() = settings.getStringOrNull(KEY_USER_ID)
        set(value) {
            if (!value.isNullOrEmpty()) settings.putString(KEY_USER_ID, value) else settings.remove(KEY_USER_ID)
        }

    val deviceId: String
        get() {
            val currentId = settings.getStringOrNull(KEY_DEVICE_ID)
            if (currentId != null) return currentId
            val newId = generateUuid()
            settings.putString(KEY_DEVICE_ID, newId)
            return newId
        }

    val deviceName: String
        get() {
            val currentName = settings.getStringOrNull(KEY_DEVICE_NAME)
            if (currentName != null) return currentName
            val newName = getDeviceName()
            settings.putString(KEY_DEVICE_NAME, newName)
            return newName
        }

    fun clear() {
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

    fun hasSession(): Boolean {
        val token = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        val url = settings.getStringOrNull(KEY_BASE_URL)
        return !token.isNullOrEmpty() && !url.isNullOrEmpty()
    }
}
