package org.jellyplex.client.domain.usecases

import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetTvShowsUseCase(private val repository: IMediaRepository) {
    operator fun invoke(): StateFlow<List<MediaItem>?> = repository.tvShows
}
