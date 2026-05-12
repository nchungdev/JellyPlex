package org.jellyplex.client.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.jellyplex.client.data.datasource.local.InMemorySessionLocalDataSource
import org.jellyplex.client.data.datasource.local.PersistentSessionLocalDataSource
import org.jellyplex.client.domain.repositories.ISessionRepository

class SessionRepository(
    private val persistentDataSource: PersistentSessionLocalDataSource,
    private val inMemoryDataSource: InMemorySessionLocalDataSource,
) : ISessionRepository {

    private fun isDemo(url: String?): Boolean {
        return url?.contains("demo.jellyfin.org") == true
    }

    private fun hasMemorySession(): Boolean = inMemoryDataSource.hasSession()

    override val isAuthenticated: Flow<Boolean> = combine(
        persistentDataSource.isAuthenticated,
        inMemoryDataSource.isAuthenticated
    ) { p, m -> p || m }

    override val baseUrl: String? get() = if (hasMemorySession()) inMemoryDataSource.baseUrl else persistentDataSource.baseUrl
    override val accessToken: String? get() = if (hasMemorySession()) inMemoryDataSource.accessToken else persistentDataSource.accessToken
    override val userId: String? get() = if (hasMemorySession()) inMemoryDataSource.userId else persistentDataSource.userId
    override val userName: String? get() = if (hasMemorySession()) inMemoryDataSource.userName else persistentDataSource.userName
    override val password: String? get() = if (hasMemorySession()) inMemoryDataSource.password else persistentDataSource.password
    
    override val deviceId: String get() = persistentDataSource.deviceId
    override val deviceName: String get() = persistentDataSource.deviceName

    override fun saveSession(url: String, token: String, userId: String?, userName: String?, password: String?) {
        // 1. Always save to Memory (Active state)
        inMemoryDataSource.baseUrl = url
        inMemoryDataSource.accessToken = token
        inMemoryDataSource.userId = userId
        inMemoryDataSource.userName = userName
        inMemoryDataSource.password = password

        // 2. Save to Persistent ONLY if it's not a demo
        if (!isDemo(url)) {
            persistentDataSource.baseUrl = url
            persistentDataSource.accessToken = token
            persistentDataSource.userId = userId
            persistentDataSource.userName = userName
            persistentDataSource.password = password
        }
    }

    override fun updateBaseUrl(url: String) {
        // Always memory
        inMemoryDataSource.baseUrl = url
        
        // Conditional persistent
        if (!isDemo(url)) {
            persistentDataSource.baseUrl = url
        }
    }

    override fun updateToken(token: String) {
        val currentUrl = baseUrl
        
        // Always memory
        inMemoryDataSource.accessToken = token
        
        // Conditional persistent
        if (!isDemo(currentUrl)) {
            persistentDataSource.accessToken = token
        }
    }

    override fun clear() {
        persistentDataSource.clear()
        inMemoryDataSource.clear()
    }

    override fun hasSession(): Boolean = persistentDataSource.hasSession() || inMemoryDataSource.hasSession()
}
