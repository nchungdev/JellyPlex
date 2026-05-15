package org.jellyplus.client.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.jellyplus.client.data.datasource.local.InMemorySessionLocalDataSource
import org.jellyplus.client.data.datasource.local.PersistentSessionLocalDataSource
import org.jellyplus.client.domain.models.Constants
import org.jellyplus.client.domain.models.RemoteServerLogin
import org.jellyplus.client.domain.repositories.ISessionRepository

class SessionRepository(
    private val persistentDataSource: PersistentSessionLocalDataSource,
    private val inMemoryDataSource: InMemorySessionLocalDataSource,
) : ISessionRepository {

    private fun isDemo(url: String?): Boolean {
        return url?.contains(Constants.DEMO_SERVER_HOST) == true
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
    override val persistDemo: Boolean get() = persistentDataSource.persistDemo
    override val remoteServerHistory: Flow<List<RemoteServerLogin>> = persistentDataSource.remoteServerHistory

    override fun saveSession(url: String, token: String, userId: String?, userName: String?, password: String?) {
        // 1. Always save to Memory (Active state)
        inMemoryDataSource.baseUrl = url
        inMemoryDataSource.accessToken = token
        inMemoryDataSource.userId = userId
        inMemoryDataSource.userName = userName
        inMemoryDataSource.password = password

        // 2. Save to Persistent ONLY if it's not a demo OR persistDemo is enabled
        if (!isDemo(url) || persistDemo) {
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
        if (!isDemo(url) || persistDemo) {
            persistentDataSource.baseUrl = url
        }
    }

    override fun setPersistDemo(enabled: Boolean) {
        persistentDataSource.persistDemo = enabled
        val currentUrl = baseUrl
        if (enabled && isDemo(currentUrl)) {
            // If enabling, persist current demo session
            persistentDataSource.baseUrl = currentUrl
            persistentDataSource.accessToken = accessToken
            persistentDataSource.userId = userId
            persistentDataSource.userName = userName
            persistentDataSource.password = password
        } else if (!enabled && isDemo(currentUrl)) {
            // If disabling while on demo, clear persistent and let authentication handle logout
            persistentDataSource.clear()
        }
    }

    override fun updateToken(token: String) {
        val currentUrl = baseUrl

        // Always memory
        inMemoryDataSource.accessToken = token

        // Conditional persistent
        if (!isDemo(currentUrl) || persistDemo) {
            persistentDataSource.accessToken = token
        }
    }

    override fun updatePassword(password: String?) {
        val currentUrl = baseUrl

        inMemoryDataSource.password = password

        if (!isDemo(currentUrl) || persistDemo) {
            persistentDataSource.password = password
        }
    }

    override fun rememberRemoteServer(url: String, username: String?) {
        if (isRemoteServerUrl(url)) {
            persistentDataSource.rememberRemoteServer(url, username)
        }
    }

    override fun clear() {
        persistentDataSource.clear()
        inMemoryDataSource.clear()
    }

    override fun hasSession(): Boolean = persistentDataSource.hasSession() || inMemoryDataSource.hasSession()

    private fun isRemoteServerUrl(url: String): Boolean {
        val host = extractHost(url)?.lowercase() ?: return false
        if (host == "localhost" || host.endsWith(".local")) return false
        if (host.contains(":")) return false
        if (host.matches(Regex("""\d{1,3}(\.\d{1,3}){3}"""))) return false
        if (!host.contains(".")) return false
        return host.any { it.isLetter() }
    }

    private fun extractHost(url: String): String? {
        val authority = url.trim()
            .substringAfter("://", url.trim())
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .substringAfter("@")

        return if (authority.startsWith("[")) {
            authority.substringAfter("[").substringBefore("]").takeIf { it.isNotBlank() }
        } else {
            authority.substringBefore(":").takeIf { it.isNotBlank() }
        }
    }
}
