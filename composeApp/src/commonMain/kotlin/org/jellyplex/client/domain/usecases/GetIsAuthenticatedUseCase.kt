package org.jellyplex.client.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class GetIsAuthenticatedUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isAuthenticated
}
