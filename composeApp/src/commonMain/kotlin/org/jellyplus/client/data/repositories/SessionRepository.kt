package org.jellyplus.client.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.jellyplus.client.data.datasource.local.InMemorySessionLocalDataSource
import org.jellyplus.client.data.datasource.local.PersistentSessionLocalDataSource
import org.jellyplus.client.domain.repositories.ISessionRepository

class SessionRepository(
    private val persistentDataSource: PersistentSessionLocalDataSource,
    private val inMemoryDataSource: InMemorySessionLocalDataSource,
) : ISessionRepository {

    private fun isDemo(url: String?): Boolean {
        return url?.contains("demo.jellyfin.org") == true
    }

    override val isAuthenticated: Flow<Boolean> = combine(
        persistentDataSource.isAuthenticated,
        inMemoryDataSource.isAuthenticated
    ) { p, m -> p || m }

    // Logic: Prioritize memory URL if set (even without session) to support connection phase
    override val baseUrl: String? 
        get() = inMemoryDataSource.baseUrl ?: persistentDataSource.baseUrl
        
    override val accessToken: String? get() = if (inMemoryDataSource.hasSession()) inMemoryDataSource.accessToken else persistentDataSource.accessToken
    override val userId: String? get() = if (inMemoryDataSource.hasSession()) inMemoryDataSource.userId else persistentDataSource.userId
    override val userName: String? get() = if (inMemoryDataSource.hasSession()) inMemoryDataSource.userName else persistentDataSource.userName
    override val password: String? get() = if (inMemoryDataSource.hasSession()) inMemoryDataSource.password else persistentDataSource.password
    
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
