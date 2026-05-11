package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.HomeContent
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetHomeContentUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(userId: String): Result<HomeContent> {
        return try {
            val resume = repository.getResumeItems(userId)
            val recentlyAdded = repository.getRecentlyAddedItems(userId)
            Result.success(HomeContent(resume, recentlyAdded))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
