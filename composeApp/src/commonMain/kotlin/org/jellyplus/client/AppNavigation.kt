package org.jellyplus.client

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.models.Constants
import org.jellyplus.client.ui.screens.AuthHomeScreen
import org.jellyplus.client.ui.screens.MainScreen
import org.jellyplus.client.ui.screens.ManualLoginScreen
import org.jellyplus.client.ui.screens.ManualServerEntryScreen
import org.jellyplus.client.ui.screens.QuickConnectScreen
import org.jellyplus.client.ui.screens.ServerSelectionScreen
import org.jellyplus.client.ui.viewmodels.DiscoveryIntent
import org.jellyplus.client.ui.viewmodels.DiscoveryState
import org.jellyplus.client.ui.viewmodels.DiscoveryViewModel
import org.jellyplus.client.ui.viewmodels.LoginIntent
import org.jellyplus.client.ui.viewmodels.LoginState
import org.jellyplus.client.ui.viewmodels.LoginViewModel
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.QuickConnectIntent
import org.jellyplus.client.ui.viewmodels.QuickConnectState
import org.jellyplus.client.ui.viewmodels.QuickConnectViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel

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
        JellyPlusLoadingLogo(modifier = Modifier.size(112.dp))
        Spacer(Modifier.height(18.dp))
        Text("JellyPlus", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(28.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun JellyPlusLoadingLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFF07101F),
            cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
        )
        drawOval(
            color = Color(0xFF00D4A8).copy(alpha = 0.08f),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.2f),
            size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.54f),
        )

        val bell = Path().apply {
            moveTo(w * 0.16f, h * 0.55f)
            quadraticTo(w * 0.15f, h * 0.10f, w * 0.50f, h * 0.08f)
            quadraticTo(w * 0.85f, h * 0.10f, w * 0.84f, h * 0.55f)
            quadraticTo(w * 0.84f, h * 0.80f, w * 0.50f, h * 0.81f)
            quadraticTo(w * 0.16f, h * 0.80f, w * 0.16f, h * 0.55f)
            close()
        }
        drawPath(bell, Color(0xFF00D4A8).copy(alpha = 0.74f))

        val shimmer = Path().apply {
            moveTo(w * 0.23f, h * 0.50f)
            quadraticTo(w * 0.24f, h * 0.22f, w * 0.50f, h * 0.20f)
            quadraticTo(w * 0.76f, h * 0.22f, w * 0.77f, h * 0.50f)
            quadraticTo(w * 0.72f, h * 0.58f, w * 0.50f, h * 0.59f)
            quadraticTo(w * 0.28f, h * 0.58f, w * 0.23f, h * 0.50f)
            close()
        }
        drawPath(shimmer, Color.White.copy(alpha = 0.14f))

        val play = Path().apply {
            moveTo(w * 0.42f, h * 0.39f)
            lineTo(w * 0.42f, h * 0.61f)
            lineTo(w * 0.63f, h * 0.50f)
            close()
        }
        drawPath(play, Color.White.copy(alpha = 0.72f))

        val dotColor = Color(0xFFE0FFF8).copy(alpha = 0.26f)
        drawCircle(dotColor, radius = w * 0.012f, center = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.30f))
        drawCircle(dotColor, radius = w * 0.010f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.17f))
        drawCircle(dotColor, radius = w * 0.012f, center = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.30f))
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
                    val demoUrl = Constants.DEMO_SERVER_URL
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
