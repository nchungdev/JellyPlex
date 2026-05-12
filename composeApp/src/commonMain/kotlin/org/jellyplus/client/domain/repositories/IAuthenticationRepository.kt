package org.jellyplus.client.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.models.AuthenticationResult

interface IAuthenticationRepository {
    val isAuthenticated: Flow<Boolean>

    suspend fun login(
        url: String,
        username: String,
        password: String,
    ): AuthenticationResult

    fun hasSession(): Boolean
    fun clearSession()
    fun updateBaseUrl(url: String)
    fun getBaseUrl(): String?
    fun getUserId(): String?
    fun setPersistDemo(enabled: Boolean)
    fun getPersistDemo(): Boolean
    suspend fun validate(): Boolean
}
