package org.jellyplex.client.data.repositories

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jellyplex.client.data.local.SessionManager
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.domain.models.QuickConnectResult

import org.jellyplex.client.domain.repositories.IQuickConnectRepository

class QuickConnectRepository(
    private val api: JellyfinApi,
    private val sessionManager: SessionManager,
) : IQuickConnectRepository {
    override suspend fun validateServer(): Boolean {
        return api.validateServer()
    }

    override suspend fun initiate(): QuickConnectResult {
        return api.initiateQuickConnect()
    }

    override fun pollStatus(secret: String): Flow<QuickConnectResult> =
        flow {
            while (true) {
                val result = api.getQuickConnectState(secret)
                if (result.authenticated && result.authenticationToken != null) {
                    // Persist session
                    sessionManager.baseUrl = api.getBaseUrl()
                    sessionManager.accessToken = result.authenticationToken
                    sessionManager.userId = result.userId
                    api.accessToken = result.authenticationToken
                }
                emit(result)
                if (result.authenticated) break
                delay(5000) // Poll every 5 seconds
            }
        }
}
