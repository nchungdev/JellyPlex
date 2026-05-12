package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.repositories.IMediaRepository

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
