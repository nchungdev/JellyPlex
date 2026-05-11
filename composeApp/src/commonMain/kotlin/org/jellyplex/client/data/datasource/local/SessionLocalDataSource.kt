package org.jellyplex.client.data.datasource.local

import kotlinx.coroutines.flow.StateFlow

interface ISessionLocalDataSource {
    val isAuthenticated: StateFlow<Boolean>
    var baseUrl: String?
    var accessToken: String?
    var userName: String?
    var password: String?
    var userId: String?

    fun clear()
    fun hasSession(): Boolean
}


class SessionLocalDataSource(
    private val persistentDataSource: ISessionLocalDataSource,
    private val inMemoryDataSource: InMemorySessionLocalDataSource,
) : ISessionLocalDataSource {

    private fun getActiveDataSource(): ISessionLocalDataSource {
        return if (persistentDataSource.baseUrl?.contains("demo.jellyfin.org") == true ||
            inMemoryDataSource.baseUrl?.contains("demo.jellyfin.org") == true
        ) {
            inMemoryDataSource
        } else {
            persistentDataSource
        }
    }

    override val isAuthenticated: StateFlow<Boolean>
        get() = getActiveDataSource().isAuthenticated

    override var baseUrl: String?
        get() = getActiveDataSource().baseUrl
        set(value) {
            if (value?.contains("demo.jellyfin.org") == true) {
                inMemoryDataSource.baseUrl = value
                inMemoryDataSource.updateAuthState()
            } else {
                persistentDataSource.baseUrl = value
            }
        }

    override var accessToken: String?
        get() = getActiveDataSource().accessToken
        set(value) {
            getActiveDataSource().accessToken = value
            if (getActiveDataSource() is InMemorySessionLocalDataSource) {
                (getActiveDataSource() as InMemorySessionLocalDataSource).updateAuthState()
            }
        }

    override var userName: String?
        get() = getActiveDataSource().userName
        set(value) {
            getActiveDataSource().userName = value
        }

    override var password: String?
        get() = getActiveDataSource().password
        set(value) {
            getActiveDataSource().password = value
        }

    override var userId: String?
        get() = getActiveDataSource().userId
        set(value) {
            getActiveDataSource().userId = value
        }

    override fun clear() {
        persistentDataSource.clear()
        inMemoryDataSource.clear()
    }

    override fun hasSession(): Boolean {
        return persistentDataSource.hasSession() || inMemoryDataSource.hasSession()
    }
}
