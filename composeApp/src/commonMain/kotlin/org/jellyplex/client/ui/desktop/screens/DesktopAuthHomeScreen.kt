package org.jellyplex.client.ui.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplex.client.ui.components.FocusableButton
import org.jellyplex.client.ui.components.FocusableOutlinedButton

@Composable
fun DesktopAuthHomeScreen(
    baseUrl: String,
    onQuickConnect: () -> Unit,
    onManualLogin: () -> Unit,
    onChangeServer: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("JellyPlex", fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(baseUrl, color = Color.Gray, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(64.dp))

        FocusableButton(
            onClick = onQuickConnect,
            modifier = Modifier.fillMaxWidth(0.5f).height(80.dp),
        ) {
            Text("Login with QuickConnect", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        FocusableOutlinedButton(
            onClick = onManualLogin,
            modifier = Modifier.fillMaxWidth(0.5f).height(80.dp),
        ) {
            Text("Manual Login", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(48.dp))

        FocusableOutlinedButton(
            onClick = onChangeServer,
            modifier = Modifier.height(56.dp).padding(horizontal = 32.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            Text("Change Server", fontSize = 18.sp)
        }
    }
}
