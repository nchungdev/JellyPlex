package org.jellyplus.client.data.discovery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.domain.discovery.IServerDiscovery

actual class PlatformServerDiscovery actual constructor() : IServerDiscovery {
    actual override fun discover(): Flow<List<DiscoveredServer>> {
        // Desktop implementation for server discovery (e.g. SSDP)
        // Returning empty list as a placeholder.
        return flowOf(emptyList())
    }
}
