package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class GetUserNameUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(): String? = repository.getUserName()
}
