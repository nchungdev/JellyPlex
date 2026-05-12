package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class ClearSessionUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke() {
        repository.clearSession()
    }

    fun setPersistDemo(enabled: Boolean) = repository.setPersistDemo(enabled)
    fun getPersistDemo(): Boolean = repository.getPersistDemo()
}
