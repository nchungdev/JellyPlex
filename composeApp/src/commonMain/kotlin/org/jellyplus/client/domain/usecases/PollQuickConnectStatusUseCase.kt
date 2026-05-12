package org.jellyplus.client.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.models.QuickConnectResult
import org.jellyplus.client.domain.repositories.IQuickConnectRepository

class PollQuickConnectStatusUseCase(private val repository: IQuickConnectRepository) {
    operator fun invoke(secret: String): Flow<QuickConnectResult> {
        return repository.pollStatus(secret)
    }
}
