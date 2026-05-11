package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class ClearSessionUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke() {
        repository.clearSession()
    }
}
