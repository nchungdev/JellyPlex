package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetItemDetailsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String, userId: String): Result<MediaItem> {
        return try {
            Result.success(repository.getItemDetails(itemId, userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
