package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.AuthenticationResult
import org.jellyplus.client.domain.repositories.IAuthenticationRepository

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
