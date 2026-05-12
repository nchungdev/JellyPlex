package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IMediaRepository

class ReportPlaybackStartUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String, playSessionId: String) {
        repository.reportPlaybackStart(itemId, playSessionId)
    }
}
