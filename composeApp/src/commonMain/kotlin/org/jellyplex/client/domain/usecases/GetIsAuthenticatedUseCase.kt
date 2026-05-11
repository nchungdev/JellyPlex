package org.jellyplex.client.domain.usecases

import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class GetIsAuthenticatedUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(): StateFlow<Boolean> = repository.isAuthenticated
}
