package org.jellyplex.client.domain.discovery

import kotlinx.serialization.Serializable

@Serializable
data class JellyfinDiscoveryResponse(
    val Address: String? = null,
    val Id: String? = null,
    val Name: String? = null,
)

