package org.jellyplex.client.domain.usecases

import org.jellyplex.client.domain.models.Person
import org.jellyplex.client.domain.repositories.IMediaRepository

class GetPeopleUseCase(private val repository: IMediaRepository) {
    suspend operator fun invoke(itemId: String): Result<List<Person>> {
        return try {
            Result.success(repository.getPeople(itemId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
