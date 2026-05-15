package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IMediaRepository

private const val DefaultMediaPageSize = 10

class RefreshMoviesUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshMovies()
}

class RefreshTvShowsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshTvShows()
}

class RefreshHomeContentUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(userId: String): Result<Unit> = repository.refreshHomeContent(userId)
}

class GetMoviesPageUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(startIndex: Int, limit: Int = DefaultMediaPageSize) =
        repository.getMoviesPage(startIndex, limit)
}

class GetTvShowsPageUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(startIndex: Int, limit: Int = DefaultMediaPageSize) =
        repository.getTvShowsPage(startIndex, limit)
}
