package org.jellyplex.client.data.discovery

import kotlinx.coroutines.flow.Flow
import org.jellyplex.client.domain.discovery.DiscoveredServer
import org.jellyplex.client.domain.discovery.IServerDiscovery

expect class PlatformServerDiscovery() : IServerDiscovery {
    override fun discover(): Flow<List<DiscoveredServer>>
}
