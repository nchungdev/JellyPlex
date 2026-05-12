package org.jellyplus.client.domain.usecases

import kotlinx.coroutines.flow.StateFlow
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetMoviesUseCase(private val repository: IMediaRepository) {
    operator fun invoke(): StateFlow<List<MediaItem>?> = repository.movies
}
