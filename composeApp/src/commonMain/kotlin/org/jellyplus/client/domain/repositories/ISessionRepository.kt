package org.jellyplus.client.domain.repositories

import kotlinx.coroutines.flow.Flow

interface ISessionRepository {
    val isAuthenticated: Flow<Boolean>
    val baseUrl: String?
    val accessToken: String?
    val userId: String?
    val userName: String?
    val password: String?
    val deviceId: String
    val deviceName: String
    val persistDemo: Boolean

    fun saveSession(
        url: String,
        token: String,
        userId: String?,
        userName: String?,
        password: String?
    )
    fun updateBaseUrl(url: String)
    fun setPersistDemo(enabled: Boolean)
    fun updateToken(token: String)
    fun clear()
    fun hasSession(): Boolean
}
