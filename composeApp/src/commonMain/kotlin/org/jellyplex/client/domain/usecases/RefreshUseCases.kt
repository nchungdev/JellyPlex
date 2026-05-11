package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IMediaRepository

class RefreshMoviesUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshMovies()
}

class RefreshTvShowsUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshTvShows()
}

class RefreshHomeContentUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(userId: String): Result<Unit> = repository.refreshHomeContent(userId)
}
