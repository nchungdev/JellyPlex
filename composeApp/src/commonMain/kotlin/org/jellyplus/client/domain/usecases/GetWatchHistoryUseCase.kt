package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetWatchHistoryUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(userId: String): List<MediaItem> = repository.getWatchHistory(userId)
}
