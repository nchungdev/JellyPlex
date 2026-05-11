package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class HasSessionUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(): Boolean = repository.hasSession()
}
