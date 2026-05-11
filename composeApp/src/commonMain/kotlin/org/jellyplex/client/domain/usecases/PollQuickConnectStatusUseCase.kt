package org.jellyplex.client.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.jellyplex.client.domain.models.QuickConnectResult
import org.jellyplex.client.domain.repositories.IQuickConnectRepository

class PollQuickConnectStatusUseCase(private val repository: IQuickConnectRepository) {
    operator fun invoke(secret: String): Flow<QuickConnectResult> {
        return repository.pollStatus(secret)
    }
}
