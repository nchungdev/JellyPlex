package org.jellyplus.client.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.jellyplus.client.data.datasource.local.MediaLocalDataSource
import org.jellyplus.client.data.datasource.remote.IAuthRemoteDataSource
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.AuthenticationResult

import org.jellyplus.client.domain.repositories.IAuthenticationRepository
import org.jellyplus.client.domain.repositories.ISessionRepository

class AuthenticationRepository(
    private val remoteDataSource: IAuthRemoteDataSource,
    private val localMediaDataSource: MediaLocalDataSource,
    private val sessionRepository: ISessionRepository,
    private val dispatchers: AppDispatchers,
) : IAuthenticationRepository {
    override val isAuthenticated: Flow<Boolean> = sessionRepository.isAuthenticated

    override suspend fun login(
        url: String,
        username: String,
        password: String,
    ): AuthenticationResult = withContext(dispatchers.io) {
        val result = remoteDataSource.login(url, username, password)

        // Persist session via SessionRepository (which handles Persistent vs Memory)
        sessionRepository.saveSession(
            url = remoteDataSource.getBaseUrl(),
            token = result.accessToken ?: "",
            userId = result.user?.id,
            userName = result.user?.name,
            password = password
        )

        result
    }

    override fun hasSession(): Boolean = sessionRepository.hasSession()

    override fun clearSession() {
        sessionRepository.clear()
        localMediaDataSource.clear()
    }

    override fun updateBaseUrl(url: String) {
        val oldUrl = sessionRepository.baseUrl
        if (oldUrl != url) {
            println("Repository: Server URL changed from $oldUrl to $url. Clearing stale session.")
            sessionRepository.clear() // Clear everything to be safe
            localMediaDataSource.clear()
        }
        remoteDataSource.updateBaseUrl(url)
        sessionRepository.updateBaseUrl(url)
    }

    override fun getBaseUrl(): String? = sessionRepository.baseUrl

    override fun getUserId(): String? = sessionRepository.userId
    override fun getUserName(): String? = sessionRepository.userName

    override fun setPersistDemo(enabled: Boolean) {
        sessionRepository.setPersistDemo(enabled)
    }

    override fun getPersistDemo(): Boolean = sessionRepository.persistDemo

    override suspend fun validate(): Boolean = withContext(dispatchers.io) {
        if (!hasSession()) return@withContext false
        try {
            // Use an endpoint that REQUIRES authentication to verify the token
            val user = remoteDataSource.validateToken()
            println("Session check: Token is valid for user ${user.name}.")
            true
        } catch (e: Exception) {
            val message = e.message ?: ""
            if (message.contains("401") || message.contains("Unauthorized")) {
                println("Session check: Token is INVALID (401). Clearing session.")
                clearSession() // CLEAR IT!
                return@withContext false
            }

            println("Session check: Network error or server unreachable ($message).")
            // If we can't reach the server, only allow offline if we have cache
            val hasCache = localMediaDataSource.movies.value != null || localMediaDataSource.tvShows.value != null
            if (!hasCache) {
                println("Session check: Unreachable and no cache. Rejecting session.")
                return@withContext false
            }
            println("Session check: Unreachable but has cache. Allowing offline access.")
            true
        }
    }
}
