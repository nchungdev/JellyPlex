package org.jellyplus.client.data.datasource.remote

import org.jellyplus.client.data.remote.models.AuthenticateByNameRequest
import org.jellyplus.client.data.remote.JellyfinApi
import org.jellyplus.client.domain.models.AuthenticationResult
import org.jellyplus.client.domain.models.UserDto

interface IAuthRemoteDataSource {
    suspend fun login(url: String, username: String, password: String): AuthenticationResult
    suspend fun validateToken(): UserDto
    fun updateBaseUrl(url: String)
    fun getBaseUrl(): String
    suspend fun changePassword(userId: String?, currentPassword: String, newPassword: String)
}

class AuthRemoteDataSource(private val api: JellyfinApi) : IAuthRemoteDataSource {

    override suspend fun login(url: String, username: String, password: String): AuthenticationResult {
        api.updateBaseUrl(url)
        return api.authenticateByName(AuthenticateByNameRequest(username, password))
    }

    override suspend fun validateToken(): UserDto {
        return api.getCurrentUser()
    }

    override fun updateBaseUrl(url: String) {
        api.updateBaseUrl(url)
    }

    override fun getBaseUrl(): String = api.baseUrl

    override suspend fun changePassword(userId: String?, currentPassword: String, newPassword: String) {
        api.updateUserPassword(userId, currentPassword, newPassword)
    }
}
