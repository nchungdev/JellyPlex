package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.QuickConnectResult
import org.jellyplus.client.domain.repositories.IQuickConnectRepository

class InitiateQuickConnectUseCase(private val repository: IQuickConnectRepository) {
    suspend operator fun invoke(): Result<QuickConnectResult> {
        return try {
            if (!repository.validateServer()) {
                return Result.failure(Exception("Cannot connect to Jellyfin server."))
            }
            Result.success(repository.initiate())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
