package org.jellyplus.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.components.FocusableButton
import org.jellyplus.client.ui.components.FocusableOutlinedButton

import org.jellyplus.client.ui.viewmodels.DiscoveryState

@Composable
fun ManualServerEntryScreen(
    state: DiscoveryState,
    onConnect: (String) -> Unit,
    onBack: () -> Unit
) {
    var url by remember { mutableStateOf("http://") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter Server URL", fontSize = 48.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Example: http://192.168.1.10:8096",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        
        Spacer(Modifier.height(48.dp))
        
        TextField(
            value = url,
            onValueChange = { url = it },
            enabled = !state.isValidatingServer,
            modifier = Modifier.fillMaxWidth(0.6f),
            placeholder = { Text("http://") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Gray
            )
        )

        if (state.error != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = state.error,
                color = Color.Red,
                fontSize = 14.sp
            )
        }
        
        Spacer(Modifier.height(64.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FocusableOutlinedButton(
                onClick = onBack,
                enabled = !state.isValidatingServer,
                modifier = Modifier.height(64.dp).width(160.dp)
            ) {
                Text("Back", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            FocusableButton(
                onClick = { onConnect(url) },
                enabled = !state.isValidatingServer && url.isNotEmpty(),
                modifier = Modifier.height(64.dp).width(200.dp)
            ) {
                if (state.isValidatingServer) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Connect", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
