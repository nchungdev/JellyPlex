package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IAuthenticationRepository
import org.jellyplus.client.domain.repositories.ISessionRepository

class ChangePasswordUseCase(
    private val authRepository: IAuthenticationRepository,
    private val sessionRepository: ISessionRepository,
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String) {
        authRepository.changePassword(currentPassword, newPassword)
        sessionRepository.updatePassword(newPassword)
    }
}
