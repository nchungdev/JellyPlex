package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetSeasonsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(seriesId: String): Result<List<MediaItem>> {
        return try {
            Result.success(repository.getSeasons(seriesId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
