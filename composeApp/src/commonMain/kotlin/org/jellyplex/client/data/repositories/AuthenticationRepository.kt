package org.jellyplex.client.data.repositories

import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.data.local.SessionManager
import org.jellyplex.client.data.remote.AuthenticateByNameRequest
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.domain.models.AuthenticationResult

import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class AuthenticationRepository(
    private val api: JellyfinApi,
    private val sessionManager: SessionManager,
) : IAuthenticationRepository {
    override val isAuthenticated: StateFlow<Boolean> = sessionManager.isAuthenticated

    override suspend fun login(
        url: String,
        username: String,
        password: String,
    ): AuthenticationResult {
        api.updateBaseUrl(url)
        val result = api.authenticateByName(AuthenticateByNameRequest(username, password))

        // Persist session
        sessionManager.baseUrl = api.getBaseUrl()
        sessionManager.accessToken = result.accessToken
        sessionManager.userName = result.user?.name
        sessionManager.userId = result.user?.id
        sessionManager.password = password

        api.accessToken = result.accessToken

        return result
    }

    override fun hasSession(): Boolean = sessionManager.hasSession()

    override fun clearSession() {
        sessionManager.clear()
    }

    override fun updateBaseUrl(url: String) {
        val oldUrl = sessionManager.baseUrl
        if (oldUrl != url) {
            println("Repository: Server URL changed from $oldUrl to $url. Clearing stale session.")
            sessionManager.clear() // Clear everything to be safe
        }
        api.updateBaseUrl(url)
        sessionManager.baseUrl = url
    }

    override fun getBaseUrl(): String? = sessionManager.baseUrl

    override fun getUserId(): String? = sessionManager.userId

    override suspend fun validate(): Boolean {
        if (!hasSession()) return false
        return try {
            // Use an endpoint that REQUIRES authentication to verify the token
            val user = api.getCurrentUser()
            println("Session check: Token is valid for user ${user.name}.")
            true
        } catch (e: Exception) {
            val message = e.message ?: ""
            if (message.contains("401") || message.contains("Unauthorized")) {
                println("Session check: Token is INVALID (401). Clearing session.")
                clearSession() // CLEAR IT!
                return false
            }
            
            println("Session check: Network error or server unreachable ($message).")
            // If we can't reach the server, only allow offline if we have cache
            val hasCache = !sessionManager.moviesCache.isNullOrEmpty() || !sessionManager.tvShowsCache.isNullOrEmpty()
            if (!hasCache) {
                println("Session check: Unreachable and no cache. Rejecting session.")
                return false
            }
            println("Session check: Unreachable but has cache. Allowing offline access.")
            true
        }
    }
}
