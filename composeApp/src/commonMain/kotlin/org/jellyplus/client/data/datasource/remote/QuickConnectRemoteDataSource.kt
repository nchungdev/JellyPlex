package org.jellyplus.client.data.datasource.remote

import org.jellyplus.client.data.remote.JellyfinApi
import org.jellyplus.client.domain.models.AuthenticationResult
import org.jellyplus.client.domain.models.QuickConnectResult

interface IQuickConnectRemoteDataSource {
    suspend fun validateServer(url: String?): Boolean
    suspend fun initiate(): QuickConnectResult
    suspend fun getStatus(secret: String): QuickConnectResult
    suspend fun authenticate(secret: String): AuthenticationResult
    fun getBaseUrl(): String
}

class QuickConnectRemoteDataSource(private val api: JellyfinApi) : IQuickConnectRemoteDataSource {
    override suspend fun validateServer(url: String?): Boolean = api.validateServer(url)
    override suspend fun initiate(): QuickConnectResult = api.initiateQuickConnect()
    override suspend fun getStatus(secret: String): QuickConnectResult = api.getQuickConnectState(secret)
    override suspend fun authenticate(secret: String): AuthenticationResult = api.authenticateWithQuickConnect(secret)
    override fun getBaseUrl(): String = api.baseUrl
}
