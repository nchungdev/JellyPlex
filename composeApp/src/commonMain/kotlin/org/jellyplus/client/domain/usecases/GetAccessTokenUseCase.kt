package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IMediaRepository

class GetAccessTokenUseCase(private val repository: IMediaRepository) {
    operator fun invoke(): String? = repository.getAccessToken()
}
