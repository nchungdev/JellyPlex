package org.jellyplex.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplex.client.ui.screens.AuthHomeScreen
import org.jellyplex.client.ui.screens.MainScreen
import org.jellyplex.client.ui.screens.ManualLoginScreen
import org.jellyplex.client.ui.screens.ManualServerEntryScreen
import org.jellyplex.client.ui.screens.QuickConnectScreen
import org.jellyplex.client.ui.screens.ServerSelectionScreen
import org.jellyplex.client.ui.viewmodels.DiscoveryIntent
import org.jellyplex.client.ui.viewmodels.DiscoveryViewModel
import org.jellyplex.client.ui.viewmodels.LoginIntent
import org.jellyplex.client.ui.viewmodels.LoginViewModel
import org.jellyplex.client.ui.viewmodels.MainViewModel
import org.jellyplex.client.ui.viewmodels.QuickConnectIntent
import org.jellyplex.client.ui.viewmodels.QuickConnectViewModel
import org.jellyplex.client.ui.viewmodels.SessionViewModel
import org.jellyplex.client.ui.viewmodels.DiscoveryState
import org.jellyplex.client.ui.viewmodels.QuickConnectState
import org.jellyplex.client.ui.viewmodels.LoginState

@Composable
fun AppMainContent(
    isValidating: Boolean,
    isAuthenticated: Boolean,
    loginMode: AuthDestination,
    mainViewModel: MainViewModel,
    sessionViewModel: SessionViewModel,
    qcViewModel: QuickConnectViewModel,
    discoveryViewModel: DiscoveryViewModel,
    loginViewModel: LoginViewModel,
    discoveryState: DiscoveryState,
    qcState: QuickConnectState,
    loginState: LoginState,
    onLoginModeChange: (AuthDestination) -> Unit
) {
    if (isValidating) {
        LoadingScreen()
    } else if (isAuthenticated) {
        MainScreen(mainViewModel = mainViewModel)
    } else {
        AuthNavigation(
            loginMode = loginMode,
            sessionViewModel = sessionViewModel,
            qcViewModel = qcViewModel,
            discoveryViewModel = discoveryViewModel,
            loginViewModel = loginViewModel,
            discoveryState = discoveryState,
            qcState = qcState,
            loginState = loginState,
            onLoginModeChange = onLoginModeChange
        )
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("JellyPlex", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(32.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AuthNavigation(
    loginMode: AuthDestination,
    sessionViewModel: SessionViewModel,
    qcViewModel: QuickConnectViewModel,
    discoveryViewModel: DiscoveryViewModel,
    loginViewModel: LoginViewModel,
    discoveryState: DiscoveryState,
    qcState: QuickConnectState,
    loginState: LoginState,
    onLoginModeChange: (AuthDestination) -> Unit
) {
    when (loginMode) {
        AuthDestination.ServerSelection -> {
            ServerSelectionScreen(
                state = discoveryState,
                onScan = { discoveryViewModel.handleIntent(DiscoveryIntent.Scan) },
                onCancelScan = { discoveryViewModel.handleIntent(DiscoveryIntent.CancelScan) },
                onServerSelected = { server ->
                    discoveryViewModel.handleIntent(
                        DiscoveryIntent.ValidateAndConnect(server.address) {
                            sessionViewModel.updateServerUrl(server.address)
                            onLoginModeChange(AuthDestination.Home)
                        }
                    )
                },
                onManualInput = { manualUrl ->
                    discoveryViewModel.handleIntent(DiscoveryIntent.ClearError)
                    if (manualUrl.isNotEmpty()) {
                        discoveryViewModel.handleIntent(
                            DiscoveryIntent.ValidateAndConnect(manualUrl) {
                                sessionViewModel.updateServerUrl(manualUrl)
                                onLoginModeChange(AuthDestination.Home)
                            }
                        )
                    } else {
                        onLoginModeChange(AuthDestination.ManualServerEntry)
                    }
                },
                onTryDemo = {
                    val demoUrl = "https://demo.jellyfin.org/stable"
                    discoveryViewModel.handleIntent(
                        DiscoveryIntent.ValidateAndConnect(demoUrl) {
                            sessionViewModel.updateServerUrl(demoUrl)
                            onLoginModeChange(AuthDestination.Home)
                        }
                    )
                }
            )
        }

        AuthDestination.ManualServerEntry -> {
            ManualServerEntryScreen(
                state = discoveryState,
                onConnect = { url ->
                    discoveryViewModel.handleIntent(
                        DiscoveryIntent.ValidateAndConnect(url) {
                            sessionViewModel.updateServerUrl(url)
                            onLoginModeChange(AuthDestination.Home)
                        }
                    )
                },
                onBack = {
                    discoveryViewModel.handleIntent(DiscoveryIntent.ClearError)
                    onLoginModeChange(AuthDestination.ServerSelection)
                }
            )
        }

        AuthDestination.Home -> {
            AuthHomeScreen(
                baseUrl = sessionViewModel.getBaseUrl(),
                onQuickConnect = {
                    onLoginModeChange(AuthDestination.QuickConnect)
                    qcViewModel.handleIntent(QuickConnectIntent.Initiate)
                },
                onManualLogin = { onLoginModeChange(AuthDestination.Manual) },
                onChangeServer = { onLoginModeChange(AuthDestination.ServerSelection) },
            )
        }

        AuthDestination.QuickConnect -> {
            QuickConnectScreen(qcState) {
                qcViewModel.handleIntent(QuickConnectIntent.Cancel)
                onLoginModeChange(AuthDestination.Home)
            }
        }

        AuthDestination.Manual -> {
            ManualLoginScreen(
                state = loginState,
                currentUrl = sessionViewModel.getBaseUrl(),
                onLogin = { url, user, pass ->
                    loginViewModel.handleIntent(LoginIntent.Login(url, user, pass))
                },
                onBack = { onLoginModeChange(AuthDestination.Home) },
            )
        }
    }
}
