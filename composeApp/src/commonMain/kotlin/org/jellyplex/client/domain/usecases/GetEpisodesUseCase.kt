package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetEpisodesUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(seriesId: String, seasonId: String): Result<List<MediaItem>> {
        return try {
            Result.success(repository.getEpisodes(seriesId, seasonId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
