package org.jellyplus.client.ui.desktop.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.domain.models.RemoteServerLogin
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import org.jellyplus.client.ui.mobile.screens.MobileAuthLogo
import org.jellyplus.client.ui.viewmodels.DiscoveryState

@Composable
fun DesktopServerSelectionScreen(
    state: DiscoveryState,
    recentServers: List<RemoteServerLogin>,
    onScan: () -> Unit,
    onCancelScan: () -> Unit,
    onServerSelected: (DiscoveredServer) -> Unit,
    onRecentServerSelected: (RemoteServerLogin) -> Unit,
    onManualInput: (String) -> Unit,
    onTryDemo: () -> Unit,
) {
    var showManualDialog by remember { mutableStateOf(false) }
    var manualUrl by remember { mutableStateOf("http://") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF181818)).padding(start = DesktopContentLeftPadding, top = 36.dp, end = DesktopContentRightPadding, bottom = 36.dp)) {
        MobileAuthLogo(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(104.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .widthIn(max = 860.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Select Server",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (state.isScanning) {
                    CircularProgressIndicator(color = Color(0xFF00D4A8), modifier = Modifier.size(24.dp))
                } else {
                    IconButton(onClick = onScan) {
                        Icon(Icons.Default.Refresh, "Rescan", tint = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            Text(
                "Choose a server from your network or add one manually",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 8.dp)
            )

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Server Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth().height(320.dp)
            ) {
                // Try Demo Card
                item {
                    DemoServerCard(onClick = onTryDemo)
                }

                items(recentServers) { server ->
                    ServerCard(
                        name = server.url.toServerName(),
                        address = "${server.url} - ${server.username}",
                        onClick = { onRecentServerSelected(server) }
                    )
                }

                // Discovered Servers
                items(state.discoveredServers) { server ->
                    ServerCard(
                        name = server.name,
                        address = server.address,
                        onClick = { onServerSelected(server) }
                    )
                }

                // Manual Input Card - Inspired by Plex 'Add Server'
                item {
                    AddServerCard(onClick = { onManualInput("") })
                }
            }
        }

        // Manual Input Dialog
        if (showManualDialog) {
            AlertDialog(
                onDismissRequest = { showManualDialog = false },
                title = { Text("Manual Server Entry") },
                text = {
                    Column {
                        Text("Enter the full URL of your Jellyfin server (e.g., http://192.168.1.10:8096)")
                        Spacer(Modifier.height(16.dp))
                        TextField(
                            value = manualUrl,
                            onValueChange = { manualUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // This would normally call a manual connect logic
                            // For now, we transition to the manual input screen or similar
                            onManualInput("")
                            showManualDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4A8))
                    ) {
                        Text("Connect", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }

        if (state.isValidatingServer) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00D4A8))
            }
        }
    }
}

@Composable
fun DemoServerCard(onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1.0f)

    Card(
        modifier = Modifier
            .height(130.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = if (isFocused) Color(0xFF00D4A8) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) Color(0xFF00D4A8).copy(alpha = 0.2f) else Color(0xFF00D4A8).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Dns, null, tint = Color(0xFF00D4A8), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text("Try Demo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Official Jellyfin Demo Server", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}

@Composable
fun ServerCard(name: String, address: String, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1.0f)

    Card(
        modifier = Modifier
            .height(130.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = if (isFocused) Color(0xFF00D4A8) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Dns, null, tint = Color(0xFF00D4A8), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(address, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}

@Composable
fun AddServerCard(onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1.0f)

    Card(
        modifier = Modifier
            .height(130.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .border(
                width = 2.dp,
                color = if (isFocused) Color(0xFF00D4A8) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text("Add Server Manually", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun String.toServerName(): String {
    return trim()
        .substringAfter("://", this)
        .substringBefore("/")
        .substringBefore("?")
        .substringBefore("#")
        .substringAfter("@")
        .substringBefore(":")
}
