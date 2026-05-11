package org.jellyplex.client.domain.repositories

import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.domain.models.AuthenticationResult

interface IAuthenticationRepository {
    val isAuthenticated: StateFlow<Boolean>

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
    suspend fun validate(): Boolean
}
