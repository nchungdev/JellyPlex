package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IMediaRepository

class ReportPlaybackStoppedUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String, playSessionId: String, positionTicks: Long) {
        repository.reportPlaybackStopped(itemId, playSessionId, positionTicks)
    }
}
