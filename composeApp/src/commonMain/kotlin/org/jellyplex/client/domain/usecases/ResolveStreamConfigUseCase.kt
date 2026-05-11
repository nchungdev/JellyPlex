package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.models.PlaybackConfig
import org.jellyplex.client.domain.repositories.IMediaRepository

class ResolveStreamConfigUseCase(private val mediaRepository: IMediaRepository) {
    suspend operator fun invoke(
        item: MediaItem,
        userId: String,
        deviceId: String
    ): PlaybackConfig? {
        return mediaRepository.resolveStreamConfig(item, userId, deviceId)
    }
}
