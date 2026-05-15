package org.jellyplus.client.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.models.QuickConnectResult

interface IQuickConnectRepository {
    suspend fun validateServer(url: String? = null): Boolean
    suspend fun initiate(): QuickConnectResult
    suspend fun authorize(code: String): Boolean
    fun pollStatus(secret: String): Flow<QuickConnectResult>
}
