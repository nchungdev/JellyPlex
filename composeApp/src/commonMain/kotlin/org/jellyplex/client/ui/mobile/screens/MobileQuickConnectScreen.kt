package org.jellyplex.client.ui.mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.jellyplex.client.ui.viewmodels.QuickConnectState

@Composable
fun MobileQuickConnectScreen(
    state: QuickConnectState,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFFFFB300))
            Text("Requesting Code...", modifier = Modifier.padding(top = 8.dp), color = Color.Gray)
        } else if (state.code != null) {
            Text("Quick Connect PIN", fontSize = 18.sp, color = Color.White)
            Text(
                text = state.code!!,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300),
                modifier = Modifier.padding(vertical = 16.dp),
            )
            Text("Enter this code on your mobile device to login.", color = Color.Gray)
        }
        if (state.error != null) {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        ) {
            Text("Back")
        }
    }
}
