package org.jellyplex.client.data.repositories

import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.data.datasource.local.IMediaLocalDataSource
import org.jellyplex.client.data.datasource.local.ISessionLocalDataSource
import org.jellyplex.client.data.datasource.remote.IAuthRemoteDataSource
import org.jellyplex.client.domain.models.AuthenticationResult

import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class AuthenticationRepository(
    private val remoteDataSource: IAuthRemoteDataSource,
    private val localMediaDataSource: IMediaLocalDataSource,
    private val sessionDataSource: ISessionLocalDataSource,
) : IAuthenticationRepository {
    override val isAuthenticated: StateFlow<Boolean> = sessionDataSource.isAuthenticated

    override suspend fun login(
        url: String,
        username: String,
        password: String,
    ): AuthenticationResult {
        val result = remoteDataSource.login(url, username, password)

        // Persist session
        sessionDataSource.baseUrl = remoteDataSource.getBaseUrl()
        sessionDataSource.accessToken = result.accessToken
        sessionDataSource.userName = result.user?.name
        sessionDataSource.userId = result.user?.id
        sessionDataSource.password = password

        return result
    }

    override fun hasSession(): Boolean = sessionDataSource.hasSession()

    override fun clearSession() {
        sessionDataSource.clear()
        localMediaDataSource.clear()
    }

    override fun updateBaseUrl(url: String) {
        val oldUrl = sessionDataSource.baseUrl
        if (oldUrl != url) {
            println("Repository: Server URL changed from $oldUrl to $url. Clearing stale session.")
            sessionDataSource.clear() // Clear everything to be safe
            localMediaDataSource.clear()
        }
        remoteDataSource.updateBaseUrl(url)
        sessionDataSource.baseUrl = url
    }

    override fun getBaseUrl(): String? = sessionDataSource.baseUrl

    override fun getUserId(): String? = sessionDataSource.userId

    override suspend fun validate(): Boolean {
        if (!hasSession()) return false
        return try {
            // Use an endpoint that REQUIRES authentication to verify the token
            val user = remoteDataSource.validateToken()
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
            val hasCache = localMediaDataSource.movies.value != null || localMediaDataSource.tvShows.value != null
            if (!hasCache) {
                println("Session check: Unreachable and no cache. Rejecting session.")
                return false
            }
            println("Session check: Unreachable but has cache. Allowing offline access.")
            true
        }
    }
}
