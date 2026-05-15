package org.jellyplus.client.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.repositories.IMediaRepository

class SetFavoriteUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(userId: String, itemId: String, favorite: Boolean) {
        repository.setFavorite(userId, itemId, favorite)
    }
}

class GetWatchLaterIdsUseCase(private val repository: IMediaRepository) {
    operator fun invoke(): Flow<Set<String>> = repository.watchLaterIds
}

class RefreshWatchLaterIdsUseCase(private val repository: IMediaRepository) {
    operator fun invoke() {
        repository.refreshWatchLaterIds()
    }
}

class SetWatchLaterUseCase(private val repository: IMediaRepository) {
    operator fun invoke(itemId: String, enabled: Boolean) {
        repository.setWatchLater(itemId, enabled)
    }
}
