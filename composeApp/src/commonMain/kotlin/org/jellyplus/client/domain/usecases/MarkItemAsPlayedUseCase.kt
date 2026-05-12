package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IMediaRepository

class MarkItemAsPlayedUseCase(
    private val repository: IMediaRepository,
    private val getUserId: GetUserIdUseCase,
) {
    suspend operator fun invoke(itemId: String) {
        val userId = getUserId() ?: return
        repository.markItemAsPlayed(userId, itemId)
    }
}
