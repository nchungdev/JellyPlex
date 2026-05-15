package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class SetPersistDemoUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(enabled: Boolean) = repository.setPersistDemo(enabled)
}
