package org.jellyplex.client.data.discovery

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.core.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import org.jellyplex.client.domain.discovery.DiscoveredServer
import org.jellyplex.client.domain.discovery.IServerDiscovery
import org.jellyplex.client.domain.discovery.JellyfinDiscoveryResponse
import java.net.NetworkInterface

actual class PlatformServerDiscovery actual constructor() : IServerDiscovery {
    private val json = Json { ignoreUnknownKeys = true }

    private fun getBroadcastAddresses(): List<InetSocketAddress> {
        val addresses = mutableListOf<InetSocketAddress>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                for (interfaceAddress in networkInterface.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null) {
                        println("DEBUG: Found broadcast address: ${broadcast.hostAddress} on ${networkInterface.name}")
                        addresses.add(InetSocketAddress(broadcast.hostAddress, 7359))
                    }
                }
            }
        } catch (e: Exception) {
            println("DEBUG: Error getting interfaces: ${e.message}")
        }
        addresses.add(InetSocketAddress("255.255.255.255", 7359))
        // Fallbacks for emulators to reach host machine
        addresses.add(InetSocketAddress("10.0.2.2", 7359)) // Standard Emulator
        addresses.add(InetSocketAddress("10.0.3.2", 7359)) // Genymotion
        return addresses.distinct()
    }

    actual override fun discover(): Flow<List<DiscoveredServer>> =
        flow {
            println("DEBUG: Starting Discovery...")
            val servers = mutableSetOf<DiscoveredServer>()
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket =
                aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 0)) {
                    broadcast = true
                }
            println("DEBUG: UDP Socket (Broadcast enabled) bound to: ${socket.localAddress}")

            try {
                val broadcastAddresses = getBroadcastAddresses()

                // Send UDP Broadcast
                for (addr in broadcastAddresses) {
                    try {
                        println("DEBUG: Sending 'Who is JellyfinServer?' to $addr")
                        socket.send(
                            Datagram(
                                packet = buildPacket { writeText("Who is JellyfinServer?") },
                                address = addr,
                            ),
                        )
                    } catch (e: Exception) {
                        println("DEBUG: Failed to send to $addr: ${e.message}")
                    }
                }

                // Listen for responses
                while (true) {
                    val datagram =
                        withTimeoutOrNull(4000) {
                            socket.receive()
                        } ?: break

                    val responseText = datagram.packet.readText()
                    println("DEBUG: Received response from ${datagram.address}: $responseText")
                    try {
                        val response =
                            json.decodeFromString<JellyfinDiscoveryResponse>(responseText)
                        if (response.Address != null) {
                            val discovered =
                                DiscoveredServer(
                                    name = response.Name ?: "Jellyfin",
                                    address = response.Address,
                                    id = response.Id ?: response.Address,
                                )
                            if (servers.add(discovered)) {
                                println("DEBUG: Emitting server: ${discovered.name}")
                                emit(servers.toList())
                            }
                        }
                    } catch (e: Exception) {
                        println("DEBUG: JSON Parse Error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Discovery Exception: ${e.message}")
            } finally {
                println("DEBUG: Closing Discovery Socket")
                socket.close()
                selectorManager.close()
            }
        }.flowOn(Dispatchers.IO)
}
