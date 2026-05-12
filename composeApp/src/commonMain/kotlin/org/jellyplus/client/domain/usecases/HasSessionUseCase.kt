package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class HasSessionUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(): Boolean = repository.hasSession()
}
