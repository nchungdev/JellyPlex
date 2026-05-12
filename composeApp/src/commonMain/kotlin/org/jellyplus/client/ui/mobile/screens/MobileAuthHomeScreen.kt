package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MobileAuthHomeScreen(
    baseUrl: String,
    onQuickConnect: () -> Unit,
    onManualLogin: () -> Unit,
    onChangeServer: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
    ) {
        Text(
            "JellyPlus",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp,
        )
        Text(
            baseUrl,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            onClick = onQuickConnect,
            modifier = Modifier.fillMaxWidth(0.85f).height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                "Login with QuickConnect",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onManualLogin,
            modifier = Modifier.fillMaxWidth(0.85f).height(64.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Manual Login", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            "Change Server",
            color = Color(0xFFFFB300),
            modifier = Modifier.clickable { onChangeServer() }.padding(8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
