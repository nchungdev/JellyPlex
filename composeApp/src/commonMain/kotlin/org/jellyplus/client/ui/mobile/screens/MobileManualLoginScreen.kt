package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.viewmodels.LoginState

@Composable
fun MobileManualLoginScreen(
    state: LoginState,
    currentUrl: String,
    suggestedUsername: String,
    onLogin: (String, String, String) -> Unit,
    onBack: () -> Unit,
) {
    var url by remember(currentUrl) { mutableStateOf(currentUrl) }
    var username by remember(currentUrl, suggestedUsername) { mutableStateOf(suggestedUsername) }
    var password by remember { mutableStateOf("") }

    MobileAuthScaffold {
        Text("Manual Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Server URL") },
            modifier = Modifier.fillMaxWidth(),
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF00D4A8),
                    unfocusedIndicatorColor = Color.Gray,
                ),
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF00D4A8),
                    unfocusedIndicatorColor = Color.Gray,
                ),
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF00D4A8),
                    unfocusedIndicatorColor = Color.Gray,
                ),
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF00D4A8))
        } else {
            MobileAuthPrimaryButton(
                text = "Login",
                onClick = { onLogin(url, username, password) },
            )

            Spacer(modifier = Modifier.height(12.dp))

            MobileAuthSecondaryButton(
                text = "Back",
                onClick = onBack,
            )
        }
        if (state.error != null) {
            Text(
                "Error: ${state.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
