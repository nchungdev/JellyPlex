package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IMediaRepository

class SaveCustomMarkerUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String, startMs: Long, endMs: Long) {
        repository.saveCustomMarker(
            itemId = itemId,
            startTicks = startMs * 10_000L,
            endTicks = endMs * 10_000L,
        )
    }
}
