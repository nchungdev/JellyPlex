package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository

class GetUserIdUseCase(private val repository: IAuthenticationRepository) {
    operator fun invoke(): String? {
        // We might need to add getUserId to IAuthenticationRepository or create a SessionRepository
        // For now, let's assume we can get it from somewhere in domain.
        // Actually, let's update IAuthenticationRepository to provide session info.
        return repository.getUserId()
    }
}
