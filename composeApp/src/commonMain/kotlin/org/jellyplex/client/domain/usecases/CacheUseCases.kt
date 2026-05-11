package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.HomeContent
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetHomeCacheUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): HomeContent? = repository.getHomeCache()
}

class SaveHomeCacheUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(content: HomeContent) = repository.saveHomeCache(content)
}

class GetMoviesCacheUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): List<MediaItem>? = repository.getMoviesCache()
}

class SaveMoviesCacheUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(items: List<MediaItem>) = repository.saveMoviesCache(items)
}

class GetTvShowsCacheUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): List<MediaItem>? = repository.getTvShowsCache()
}

class SaveTvShowsCacheUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(items: List<MediaItem>) = repository.saveTvShowsCache(items)
}
