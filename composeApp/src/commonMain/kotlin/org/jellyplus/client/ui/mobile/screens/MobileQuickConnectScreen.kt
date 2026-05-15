package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.viewmodels.QuickConnectState

@Composable
fun MobileQuickConnectScreen(
    state: QuickConnectState,
    onBack: () -> Unit,
) {
    MobileAuthScaffold {
        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF00D4A8))
            Text("Requesting Code...", modifier = Modifier.padding(top = 8.dp), color = Color.Gray)
        } else if (state.code != null) {
            Text("Quick Connect PIN", fontSize = 18.sp, color = Color.White)
            Text(
                text = state.code!!,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D4A8),
                modifier = Modifier.padding(vertical = 16.dp),
            )
            Text("Enter this code on your mobile device to login.", color = Color.Gray)
        }
        if (state.error != null) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        MobileAuthSecondaryButton(
            text = "Back",
            onClick = onBack,
        )
    }
}
