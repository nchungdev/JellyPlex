package org.jellyplus.client.data.datasource.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySessionLocalDataSource {
    var baseUrl: String? = null
        set(value) { field = value; updateAuthState() }
    var accessToken: String? = null
        set(value) { field = value; updateAuthState() }
    var userName: String? = null
    var password: String? = null
    var userId: String? = null

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun clear() {
        baseUrl = null
        accessToken = null
        userName = null
        password = null
        userId = null
        updateAuthState()
    }

    fun hasSession(): Boolean {
        return !accessToken.isNullOrEmpty() && !baseUrl.isNullOrEmpty()
    }

    fun updateAuthState() {
        _isAuthenticated.value = hasSession()
    }
}
