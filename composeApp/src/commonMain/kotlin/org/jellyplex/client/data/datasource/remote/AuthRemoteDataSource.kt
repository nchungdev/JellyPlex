package org.jellyplex.client.data.datasource.remote

import org.jellyplex.client.data.remote.AuthenticateByNameRequest
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.domain.models.AuthenticationResult
import org.jellyplex.client.domain.models.UserDto

interface IAuthRemoteDataSource {
    suspend fun login(url: String, username: String, password: String): AuthenticationResult
    suspend fun validateToken(): UserDto
    fun updateBaseUrl(url: String)
    fun getBaseUrl(): String
}

class AuthRemoteDataSource(private val api: JellyfinApi) : IAuthRemoteDataSource {

    override suspend fun login(url: String, username: String, password: String): AuthenticationResult {
        api.updateBaseUrl(url)
        val result = api.authenticateByName(AuthenticateByNameRequest(username, password))
        api.accessToken = result.accessToken
        return result
    }

    override suspend fun validateToken(): UserDto {
        return api.getCurrentUser()
    }

    override fun updateBaseUrl(url: String) {
        api.updateBaseUrl(url)
    }

    override fun getBaseUrl(): String = api.getBaseUrl()
}
