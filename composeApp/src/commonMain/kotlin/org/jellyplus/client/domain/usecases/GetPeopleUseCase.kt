package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.models.Person
import org.jellyplus.client.domain.repositories.IMediaRepository

class GetPeopleUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String): Result<List<Person>> {
        return try {
            Result.success(repository.getPeople(itemId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
