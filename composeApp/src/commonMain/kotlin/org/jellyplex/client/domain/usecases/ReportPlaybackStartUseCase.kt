package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IMediaRepository

class ReportPlaybackStartUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String, playSessionId: String) {
        repository.reportPlaybackStart(itemId, playSessionId)
    }
}
