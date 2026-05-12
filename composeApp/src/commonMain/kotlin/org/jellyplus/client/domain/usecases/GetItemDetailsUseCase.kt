package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetItemDetailsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String, userId: String): Result<MediaItem> {
        return try {
            Result.success(repository.getItemDetails(itemId, userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
