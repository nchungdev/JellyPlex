package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class SearchItemsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(query: String): Result<List<MediaItem>> {
        if (query.isBlank()) return Result.success(emptyList())
        return try {
            Result.success(repository.searchItems(query))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
