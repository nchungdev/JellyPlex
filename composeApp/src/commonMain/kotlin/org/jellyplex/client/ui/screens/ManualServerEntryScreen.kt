package org.jellyplex.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplex.client.ui.components.FocusableButton
import org.jellyplex.client.ui.components.FocusableOutlinedButton

@Composable
fun ManualServerEntryScreen(
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
        
        Spacer(Modifier.height(64.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            FocusableOutlinedButton(
                onClick = onBack,
                modifier = Modifier.height(64.dp).width(160.dp)
            ) {
                Text("Back", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            FocusableButton(
                onClick = { onConnect(url) },
                modifier = Modifier.height(64.dp).width(200.dp)
            ) {
                Text("Connect", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
