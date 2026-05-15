package org.jellyplus.client.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.models.RemoteServerLogin

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
    val remoteServerHistory: Flow<List<RemoteServerLogin>>

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
    fun updatePassword(password: String?)
    fun rememberRemoteServer(url: String, username: String?)
    fun clear()
    fun hasSession(): Boolean
}
