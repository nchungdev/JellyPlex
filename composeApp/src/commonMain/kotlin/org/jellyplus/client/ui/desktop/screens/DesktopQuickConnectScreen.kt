package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.viewmodels.QuickConnectState

@Composable
fun DesktopQuickConnectScreen(
    state: QuickConnectState,
    onBack: () -> Unit,
) {
    org.jellyplus.client.AppBackHandler(enabled = true, onBack = onBack)

    DesktopAuthScaffold(maxContentWidth = 4000.dp) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Color(0xFF00D4A8), modifier = Modifier.size(42.dp))
                Text("Requesting Code...", modifier = Modifier.padding(top = 12.dp), color = Color.Gray, fontSize = 15.sp)
            } else if (state.code != null) {
                Text("Quick Connect PIN", fontSize = 20.sp, color = Color.White)
                Text(
                    text = state.code!!,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D4A8),
                    modifier = Modifier.padding(vertical = 22.dp),
                )
                Text(
                    "Enter this code on your mobile device to login.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }
    }
}
