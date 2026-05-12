package org.jellyplus.client.data.discovery

import kotlinx.coroutines.flow.Flow
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.domain.discovery.IServerDiscovery

expect class PlatformServerDiscovery() : IServerDiscovery {
    override fun discover(): Flow<List<DiscoveredServer>>
}
