package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class ValidateSessionUseCase(
    private val repository: IAuthenticationRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.validate()
    }
}
