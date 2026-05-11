package org.jellyplex.client.domain.usecases

import kotlinx.coroutines.flow.Flow
import org.jellyplex.client.domain.discovery.DiscoveredServer
import org.jellyplex.client.domain.discovery.IServerDiscovery

class DiscoverServersUseCase(private val discovery: IServerDiscovery) {
    operator fun invoke(): Flow<List<DiscoveredServer>> {
        return discovery.discover()
    }
}
