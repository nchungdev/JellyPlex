package org.jellyplus.client.domain.discovery

import kotlinx.coroutines.flow.Flow

data class DiscoveredServer(
    val name: String,
    val address: String,
    val id: String,
)

interface IServerDiscovery {
    fun discover(): Flow<List<DiscoveredServer>>
}
