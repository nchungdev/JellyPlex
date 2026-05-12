package org.jellyplex.client.data.repositories

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jellyplex.client.data.datasource.remote.IQuickConnectRemoteDataSource
import org.jellyplex.client.domain.models.QuickConnectResult
import org.jellyplex.client.domain.repositories.IQuickConnectRepository
import org.jellyplex.client.domain.repositories.ISessionRepository

class QuickConnectRepository(
    private val remoteDataSource: IQuickConnectRemoteDataSource,
    private val sessionRepository: ISessionRepository,
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
                        val currentUrl = apiBaseUrl.ifEmpty { sessionRepository.baseUrl ?: "" }

                        println("QuickConnect: Persisting -> URL: '$currentUrl', Token: '${result.authenticationToken.take(5)}...'")

                        if (currentUrl.isNotEmpty()) {
                            sessionRepository.saveSession(
                                url = currentUrl,
                                token = result.authenticationToken,
                                userId = result.userId,
                                userName = null, // Will be fetched later or not needed
                                password = null
                            )
                            println("QuickConnect: Session saved. Auth state: ${sessionRepository.hasSession()}")
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
