package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.AuthenticationResult
import org.jellyplex.client.domain.repositories.IAuthenticationRepository

class LoginUseCase(private val repository: IAuthenticationRepository) {
    suspend operator fun invoke(
        url: String,
        username: String,
        password: String,
    ): Result<AuthenticationResult> {
        return try {
            val result = repository.login(url, username, password)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
