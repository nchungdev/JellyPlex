package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IQuickConnectRepository

class ValidateServerUseCase(private val repository: IQuickConnectRepository) {
    suspend operator fun invoke(): Boolean = repository.validateServer()
}
