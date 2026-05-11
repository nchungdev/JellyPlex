package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class UpdateBaseUrlUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(url: String) = repository.updateBaseUrl(url)
}
