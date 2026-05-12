package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.IntroMarker
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetIntroMarkersUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String): List<IntroMarker> = repository.getIntroMarkers(itemId)
}
