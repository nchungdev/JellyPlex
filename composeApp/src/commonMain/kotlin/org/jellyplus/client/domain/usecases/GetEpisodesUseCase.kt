package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetEpisodesUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(seriesId: String, seasonId: String): Result<List<MediaItem>> {
        return try {
            Result.success(repository.getEpisodes(seriesId, seasonId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
