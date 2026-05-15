package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IQuickConnectRepository

class AuthorizeQuickConnectUseCase(private val repository: IQuickConnectRepository) {
    suspend operator fun invoke(code: String): Boolean = repository.authorize(code)
}
