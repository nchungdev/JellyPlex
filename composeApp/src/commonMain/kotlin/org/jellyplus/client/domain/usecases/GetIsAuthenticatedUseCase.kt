package org.jellyplus.client.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class GetIsAuthenticatedUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isAuthenticated
}
