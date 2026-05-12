package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.viewmodels.QuickConnectState

@Composable
fun DesktopQuickConnectScreen(
    state: QuickConnectState,
    onBack: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFFFFB300), modifier = Modifier.size(64.dp))
            Text("Requesting Code...", modifier = Modifier.padding(top = 16.dp), color = Color.Gray, fontSize = 20.sp)
        } else if (state.code != null) {
            Text("Quick Connect PIN", fontSize = 24.sp, color = Color.White)
            Text(
                text = state.code!!,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300),
                modifier = Modifier.padding(vertical = 32.dp),
            )
            Text("Enter this code on your mobile device to login.", color = Color.Gray, fontSize = 18.sp)
        }
        if (state.error != null) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(48.dp))
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.height(64.dp).width(200.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        ) {
            Text("Back", fontSize = 20.sp)
        }
    }
}
