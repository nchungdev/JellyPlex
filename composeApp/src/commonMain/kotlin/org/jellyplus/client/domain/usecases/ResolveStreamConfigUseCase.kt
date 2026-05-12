package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.domain.repositories.IMediaRepository

class ResolveStreamConfigUseCase(private val mediaRepository: IMediaRepository) {
    suspend operator fun invoke(
        item: MediaItem,
        userId: String,
        deviceId: String
    ): PlaybackConfig? {
        return mediaRepository.resolveStreamConfig(item, userId, deviceId)
    }
}
