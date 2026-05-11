package org.jellyplex.client.data.datasource.remote

import kotlinx.coroutines.flow.Flow
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.domain.models.QuickConnectResult

interface IQuickConnectRemoteDataSource {
    suspend fun validateServer(url: String?): Boolean
    suspend fun initiate(): QuickConnectResult
    suspend fun getStatus(secret: String): QuickConnectResult
    fun getBaseUrl(): String
}

class QuickConnectRemoteDataSource(private val api: JellyfinApi) : IQuickConnectRemoteDataSource {
    override suspend fun validateServer(url: String?): Boolean = api.validateServer(url)
    override suspend fun initiate(): QuickConnectResult = api.initiateQuickConnect()
    override suspend fun getStatus(secret: String): QuickConnectResult = api.getQuickConnectState(secret)
    override fun getBaseUrl(): String = api.getBaseUrl()
}
