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
                    var result = remoteDataSource.getStatus(secret)

                    if (result.authenticated && result.authenticationToken == null) {
                        println("QuickConnect: Authenticated but token missing. Attempting explicit exchange...")
                        try {
                            val authResult = remoteDataSource.authenticate(secret)
                            if (authResult.accessToken != null) {
                                println("QuickConnect: Explicit exchange SUCCESS!")
                                result = result.copy(
                                    authenticationToken = authResult.accessToken,
                                    userId = authResult.user?.id
                                )
                            }
                        } catch (e: Exception) {
                            println("QuickConnect: Explicit exchange failed - ${e.message}")
                        }
                    }

                    println("QuickConnect: Final status - Authenticated: ${result.authenticated}, HasToken: ${result.authenticationToken != null}")
                    emit(result)

                    if (result.authenticated && result.authenticationToken != null) {
                        val apiBaseUrl = remoteDataSource.getBaseUrl()
                        val currentUrl = apiBaseUrl.ifEmpty { sessionDataSource.baseUrl ?: "" }

                        println("QuickConnect: Persisting -> URL: '$currentUrl', Token: '${result.authenticationToken.take(5)}...'")

                        if (currentUrl.isNotEmpty()) {
                            // Cập nhật URL trước, sau đó đến Token để trigger isAuthenticated đúng
                            sessionDataSource.baseUrl = currentUrl
                            sessionDataSource.accessToken = result.authenticationToken
                            sessionDataSource.userId = result.userId
                            println("QuickConnect: Session saved. Auth state: ${sessionDataSource.hasSession()}")
                        } else {
                            println("QuickConnect: [ERROR] Cannot navigate to Main because BaseURL is still empty!")
                        }
                        break
                    }
                } catch (e: Exception) {
                    println("QuickConnect: Polling error - ${e.message}")
                }
                delay(2000)
            }
        }
}
