package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class ValidateSessionUseCase(
    private val repository: IAuthenticationRepository
) {
    suspend operator fun invoke(): Boolean {
        return repository.validate()
    }
}
