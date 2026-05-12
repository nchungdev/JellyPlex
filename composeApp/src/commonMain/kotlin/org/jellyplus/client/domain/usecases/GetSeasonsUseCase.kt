package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetSeasonsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(seriesId: String): Result<List<MediaItem>> {
        return try {
            Result.success(repository.getSeasons(seriesId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
