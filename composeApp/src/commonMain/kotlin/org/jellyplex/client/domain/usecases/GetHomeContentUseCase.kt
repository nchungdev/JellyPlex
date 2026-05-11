package org.jellyplex.client.domain.usecases

import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.domain.models.HomeContent
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetHomeContentUseCase(private val repository: IMediaRepository) {
    operator fun invoke(): StateFlow<HomeContent?> = repository.homeContent
}
