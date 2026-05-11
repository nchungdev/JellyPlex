package org.jellyplex.client.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.jellyplex.client.domain.models.QuickConnectResult

interface IQuickConnectRepository {
    suspend fun validateServer(url: String? = null): Boolean
    suspend fun initiate(): QuickConnectResult
    fun pollStatus(secret: String): Flow<QuickConnectResult>
}
