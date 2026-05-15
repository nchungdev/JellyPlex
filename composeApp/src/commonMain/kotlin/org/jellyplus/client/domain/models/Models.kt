package org.jellyplus.client.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicSystemInfo(
    @SerialName("ServerName") val serverName: String? = null,
    @SerialName("Version") val version: String? = null,
    @SerialName("Id") val id: String? = null,
)

@Serializable
data class AuthenticationResult(
    @SerialName("AccessToken") val accessToken: String? = null,
    @SerialName("User") val user: UserDto? = null,
)

@Serializable
data class UserDto(
    @SerialName("Id") val id: String? = null,
    @SerialName("Name") val name: String? = null,
)

@Serializable
data class RemoteServerLogin(
    val url: String,
    val username: String,
)

@Serializable
data class QuickConnectResult(
    @SerialName("Secret") val secret: String? = null,
    @SerialName("Code") val code: String? = null,
    @SerialName("Authenticated") val authenticated: Boolean = false,
    @SerialName("AuthenticationToken") val authenticationToken: String? = null,
    @SerialName("UserId") val userId: String? = null,
)
