package org.jellyplex.client.data.repositories

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jellyplex.client.data.datasource.local.ISessionLocalDataSource
import org.jellyplex.client.data.datasource.remote.IQuickConnectRemoteDataSource
import org.jellyplex.client.domain.models.QuickConnectResult
import org.jellyplex.client.domain.repositories.IQuickConnectRepository

class QuickConnectRepository(
    private val remoteDataSource: IQuickConnectRemoteDataSource,
    private val sessionDataSource: ISessionLocalDataSource,
) : IQuickConnectRepository {
    override suspend fun validateServer(url: String?): Boolean {
        return remoteDataSource.validateServer(url)
    }

    override suspend fun initiate(): QuickConnectResult {
        return remoteDataSource.initiate()
    }

    override fun pollStatus(secret: String): Flow<QuickConnectResult> =
        flow {
            println("QuickConnect: Starting polling with secret: $secret")
            while (true) {
                try {
                    val result = remoteDataSource.getStatus(secret)
                    println("QuickConnect: Polling status - Authenticated: ${result.authenticated}, HasToken: ${result.authenticationToken != null}")

                    emit(result)

                    if (result.authenticated && result.authenticationToken != null) {
                        println("QuickConnect: Auth SUCCESS! Token received. Persisting...")
                        val currentUrl = remoteDataSource.getBaseUrl().ifEmpty { sessionDataSource.baseUrl ?: "" }
                        sessionDataSource.baseUrl = currentUrl
                        sessionDataSource.accessToken = result.authenticationToken
                        sessionDataSource.userId = result.userId
                        break
                    }
                } catch (e: Exception) {
                    println("QuickConnect: Polling error - ${e.message}")
                }
                delay(2000)
            }
        }
}
