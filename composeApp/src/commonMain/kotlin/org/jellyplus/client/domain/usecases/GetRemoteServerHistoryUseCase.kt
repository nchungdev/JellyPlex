package org.jellyplus.client.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.models.RemoteServerLogin
import org.jellyplus.client.domain.repositories.ISessionRepository

class GetRemoteServerHistoryUseCase(private val repository: ISessionRepository) {
    operator fun invoke(): Flow<List<RemoteServerLogin>> = repository.remoteServerHistory
}
