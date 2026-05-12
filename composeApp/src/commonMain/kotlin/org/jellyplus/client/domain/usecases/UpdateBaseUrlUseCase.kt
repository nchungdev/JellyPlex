package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class UpdateBaseUrlUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(url: String) = repository.updateBaseUrl(url)
}
