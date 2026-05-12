package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IMediaRepository

class ReportPlaybackStoppedUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String, playSessionId: String, positionTicks: Long) {
        repository.reportPlaybackStopped(itemId, playSessionId, positionTicks)
    }
}
