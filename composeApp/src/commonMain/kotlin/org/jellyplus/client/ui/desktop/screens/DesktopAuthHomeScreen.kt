package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.layout.Spacer
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
import org.jellyplus.client.ui.components.FocusableButton
import org.jellyplus.client.ui.components.FocusableOutlinedButton

@Composable
fun DesktopAuthHomeScreen(
    baseUrl: String,
    onQuickConnect: () -> Unit,
    onManualLogin: () -> Unit,
    onChangeServer: () -> Unit,
) {
    DesktopAuthScaffold(maxContentWidth = 420.dp) {
        Text("JellyPlus", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(baseUrl, color = Color.Gray, fontSize = 15.sp)

        Spacer(modifier = Modifier.height(42.dp))

        FocusableButton(
            onClick = onQuickConnect,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("Login with QuickConnect", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        FocusableOutlinedButton(
            onClick = onManualLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("Manual Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(34.dp))

        FocusableOutlinedButton(
            onClick = onChangeServer,
            modifier = Modifier.height(46.dp).padding(horizontal = 32.dp),
            shape = RoundedCornerShape(28.dp),
        ) {
            Text("Change Server", fontSize = 15.sp)
        }
    }
}
