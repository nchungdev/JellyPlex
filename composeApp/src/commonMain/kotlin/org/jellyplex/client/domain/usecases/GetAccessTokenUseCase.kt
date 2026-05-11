package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.repositories.IMediaRepository

class GetAccessTokenUseCase(private val repository: IMediaRepository) {
    operator fun invoke(): String? = repository.getAccessToken()
}
