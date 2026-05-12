package org.jellyplus.client.data.discovery

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.domain.discovery.IServerDiscovery

actual class PlatformServerDiscovery actual constructor() : IServerDiscovery {
    actual override fun discover(): Flow<List<DiscoveredServer>> =
        flow {
            // Web/Wasm cannot use UDP. Placeholder for manual entry or mDNS if supported.
            emit(emptyList())
        }
}
