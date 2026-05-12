package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.ui.viewmodels.DiscoveryState

@Composable
fun MobileServerSelectionScreen(
    state: DiscoveryState,
    onScan: () -> Unit,
    onCancelScan: () -> Unit,
    onServerSelected: (DiscoveredServer) -> Unit,
    onManualInput: (String) -> Unit,
    onTryDemo: () -> Unit,
) {
    var manualUrl by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedServerName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.discoveredServers, state.isScanning) {
        if (!state.isScanning && state.discoveredServers.size == 1) {
            manualUrl = state.discoveredServers.first().address
            selectedServerName = state.discoveredServers.first().name
        }
        if (!state.isScanning && state.discoveredServers.size > 1) {
            isDropdownExpanded = true
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Jellyfin Server",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
        )
        Text(
            text = "Enter address or scan network",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 12.dp),
        )

        Spacer(modifier = Modifier.height(64.dp))

        OutlinedTextField(
            value = if (state.isScanning) "Scanning..." else manualUrl,
            onValueChange = { manualUrl = it },
            label = { Text(selectedServerName ?: "Server Address") },
            enabled = !state.isScanning,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFB300),
                    focusedLabelColor = Color(0xFFFFB300),
                    cursorColor = Color(0xFFFFB300),
                ),
            trailingIcon = {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFFFFB300),
                        strokeWidth = 2.dp,
                    )
                } else {
                    IconButton(onClick = onScan) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    }
                }
            },
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = state.error ?: "",
            color = Color.Red,
            fontSize = 14.sp,
            modifier = Modifier.height(36.dp).padding(bottom = 8.dp),
        )

        Button(
            onClick = { onManualInput(manualUrl) },
            enabled = !state.isScanning && !state.isValidatingServer && manualUrl.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color.Black,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            if (state.isValidatingServer) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Connect", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onTryDemo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Demo Server", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
        }

        if (state.discoveredServers.isNotEmpty() && !state.isScanning) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("Discovered Servers", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
            state.discoveredServers.forEach { server ->
                Surface(
                    onClick = {
                        manualUrl = server.address
                        selectedServerName = server.name
                        onServerSelected(server)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(server.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(server.address, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
