package org.jellyplus.client.data.datasource.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import org.jellyplus.client.data.db.JellyPlusDatabase
import org.jellyplus.client.data.local.DatabaseDriverFactory
import org.jellyplus.client.domain.models.RemoteServerLogin

class AppDatabaseLocalDataSource(
    driverFactory: DatabaseDriverFactory,
) {
    private val database = driverFactory.createDriver()?.let { JellyPlusDatabase(it) }
    private val queries = database?.jellyPlusDatabaseQueries

    private val _remoteServerHistory = MutableStateFlow(loadRemoteServerHistory())
    val remoteServerHistory: StateFlow<List<RemoteServerLogin>> = _remoteServerHistory.asStateFlow()

    private val watchLaterFlows = mutableMapOf<String, MutableStateFlow<Set<String>>>()

    fun rememberRemoteServer(url: String, username: String) {
        val normalizedUrl = url.trim().trimEnd('/')
        val normalizedUsername = username.trim()
        if (normalizedUrl.isEmpty() || normalizedUsername.isEmpty()) return

        queries?.upsertServerLogin(
            url = normalizedUrl,
            username = normalizedUsername,
            last_login_at = Clock.System.now().toEpochMilliseconds(),
        )
        _remoteServerHistory.value = loadRemoteServerHistory()
    }

    fun watchLaterIds(serverUrl: String, userId: String): StateFlow<Set<String>> {
        val key = watchLaterKey(serverUrl, userId)
        return watchLaterFlows.getOrPut(key) {
            MutableStateFlow(loadWatchLaterIds(serverUrl, userId))
        }.asStateFlow()
    }

    fun setWatchLater(serverUrl: String, userId: String, itemId: String, enabled: Boolean) {
        if (serverUrl.isBlank() || userId.isBlank() || itemId.isBlank()) return
        if (enabled) {
            queries?.insertWatchLater(
                server_url = serverUrl,
                user_id = userId,
                item_id = itemId,
                added_at = Clock.System.now().toEpochMilliseconds(),
            )
        } else {
            queries?.deleteWatchLater(server_url = serverUrl, user_id = userId, item_id = itemId)
        }
        watchLaterFlows[watchLaterKey(serverUrl, userId)]?.value = loadWatchLaterIds(serverUrl, userId)
    }

    fun loadWatchLaterIds(serverUrl: String, userId: String): Set<String> {
        if (serverUrl.isBlank() || userId.isBlank()) return emptySet()
        return queries?.selectWatchLaterIds(server_url = serverUrl, user_id = userId)
            ?.executeAsList()
            ?.toSet()
            ?: emptySet()
    }

    private fun loadRemoteServerHistory(): List<RemoteServerLogin> {
        return queries?.selectServerLoginHistory()
            ?.executeAsList()
            ?.map { RemoteServerLogin(url = it.url, username = it.username) }
            ?: emptyList()
    }

    private fun watchLaterKey(serverUrl: String, userId: String): String = "$serverUrl\n$userId"
}
