package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IMediaRepository

class ReportPlaybackProgressUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean = false
    ) {
        repository.reportPlaybackProgress(itemId, playSessionId, positionTicks, isPaused, isMuted)
    }
}
