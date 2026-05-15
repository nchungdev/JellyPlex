package org.jellyplus.client.ui.screens.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import org.jellyplus.client.ui.screens.SettingsCategoryRow
import org.jellyplus.client.ui.screens.SettingsInfoRow
import org.jellyplus.client.ui.screens.settings.components.PreferenceGroupCard
import org.jellyplus.client.ui.screens.settings.components.PreferenceInfoRow
import org.jellyplus.client.ui.viewmodels.AccountSettingsState

// ── Account overview (nav items) ─────────────────────────────────────────────

@Composable
internal fun AccountPreferencesPanel(
    userName: String,
    serverHost: String,
    onChangePasswordTap: () -> Unit,
    onQuickConnectTap: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PreferenceGroupCard("Account") {
            SettingsInfoRow(icon = Icons.Default.Person, title = "Signed in as", value = userName)
            SettingsInfoRow(icon = Icons.Default.Info, title = "Server", value = serverHost)
        }
    }

    // Nav rows — same card style as root SettingsCategoryRow
    SettingsCategoryRow(
        icon = Icons.Default.Lock,
        title = "Change password",
        subtitle = "Update your Jellyfin account password",
        onClick = onChangePasswordTap,
    )
    SettingsCategoryRow(
        icon = Icons.Default.QrCodeScanner,
        title = "Quick Connect",
        subtitle = "Authorize another device to sign in as your account",
        onClick = onQuickConnectTap,
    )
}

// ── Change password detail form ───────────────────────────────────────────────

@Composable
internal fun ChangePasswordPanel(
    state: AccountSettingsState,
    onChangePassword: (String, String, String) -> Unit,
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        PreferenceGroupCard("Change password", showDivider = false) {
            AccountPasswordField("Current password", currentPassword) { currentPassword = it }
            AccountPasswordField("New password", newPassword) { newPassword = it }
            AccountPasswordField("Confirm password", confirmPassword) { confirmPassword = it }
            state.passwordMessage?.let {
                val isSuccess = it.contains("updated", ignoreCase = true)
                Text(
                    it,
                    color = if (isSuccess) MaterialTheme.colorScheme.primary else Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Button(
                onClick = { onChangePassword(currentPassword, newPassword, confirmPassword) },
                enabled = !state.isChangingPassword,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) {
                Text(
                    if (state.isChangingPassword) "Updating…" else "Update password",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Quick Connect detail form ─────────────────────────────────────────────────

@Composable
internal fun QuickConnectPanel(
    state: AccountSettingsState,
    onAuthorizeQuickConnect: (String) -> Unit,
) {
    var quickConnectCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        PreferenceGroupCard("Quick Connect", showDivider = false) {
            PreferenceInfoRow(
                "Authorize another device",
                "Enter the Quick Connect code shown on another Jellyfin device to let it sign in as this account.",
            )
            TextField(
                value = quickConnectCode,
                onValueChange = { quickConnectCode = it.uppercase() },
                placeholder = { Text("Code", color = Color.White.copy(alpha = 0.35f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp),
            )
            state.quickConnectMessage?.let {
                val isSuccess = it.contains("authorized", ignoreCase = true)
                Text(
                    it,
                    color = if (isSuccess) MaterialTheme.colorScheme.primary else Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Button(
                onClick = { onAuthorizeQuickConnect(quickConnectCode) },
                enabled = !state.isAuthorizingQuickConnect,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            ) {
                Text(
                    if (state.isAuthorizingQuickConnect) "Authorizing…" else "Authorize device",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Shared password field ─────────────────────────────────────────────────────

@Composable
private fun AccountPasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.White.copy(alpha = 0.35f)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
    )
}
