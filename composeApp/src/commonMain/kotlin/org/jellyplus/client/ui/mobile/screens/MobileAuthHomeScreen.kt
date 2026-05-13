package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    MobileAuthScaffold {
        Text(
            "JellyPlus",
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
        Text(
            baseUrl,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(56.dp))

        MobileAuthPrimaryButton(
            text = "Login with QuickConnect",
            onClick = onQuickConnect,
        )

        Spacer(modifier = Modifier.height(16.dp))

        MobileAuthSecondaryButton(
            text = "Manual Login",
            onClick = onManualLogin,
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Change Server",
            color = Color(0xFFFFB300),
            modifier = Modifier.clickable { onChangeServer() }.padding(8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
