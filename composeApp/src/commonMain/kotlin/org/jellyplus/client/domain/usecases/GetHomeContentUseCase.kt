package org.jellyplus.client.domain.usecases

import kotlinx.coroutines.flow.StateFlow
import org.jellyplus.client.domain.models.HomeContent
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetHomeContentUseCase(private val repository: IMediaRepository) {
    operator fun invoke(): StateFlow<HomeContent?> = repository.homeContent
}
