package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetTvShowsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): Result<List<MediaItem>> {
        return try {
            Result.success(repository.getTvShows())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
