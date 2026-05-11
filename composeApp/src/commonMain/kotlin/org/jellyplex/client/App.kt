package org.jellyplex.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplex.client.di.appModule
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

@Composable
fun App() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin { modules(appModule()) }
    }

    AppContent()
}

enum class AuthDestination {
    ServerSelection,
    ManualServerEntry,
    Home,
    QuickConnect,
    Manual
}

@Composable
fun AppContent() {
    val uiType = getUiType()

    if (uiType == UiType.Mobile) {
        OrientationEffect(ScreenOrientation.Portrait)
    }

    val sessionViewModel: SessionViewModel = koinViewModel()
    val qcViewModel: QuickConnectViewModel = koinViewModel()
    val loginViewModel: LoginViewModel = koinViewModel()
    val discoveryViewModel: DiscoveryViewModel = koinViewModel()
    val mainViewModel: MainViewModel = koinViewModel()

    val sessionState by sessionViewModel.uiState.collectAsState()
    val isAuthenticated = sessionState.isAuthenticated
    val isValidating = sessionState.isValidating

    LaunchedEffect(isAuthenticated, isValidating) {
        println("App: State - isAuthenticated: $isAuthenticated, isValidating: $isValidating")
    }

    val qcState by qcViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    val discoveryState by discoveryViewModel.state.collectAsState()

    var loginMode by remember { mutableStateOf(AuthDestination.ServerSelection) }

    // Reset login mode when logged out or handle successful login
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            println("App: Authenticated! Moving to Home and loading data.")
            loginMode = AuthDestination.Home
            mainViewModel.loadData()
        } else {
            println("App: Not authenticated. Resetting to Server Selection.")
            loginMode = AuthDestination.ServerSelection
        }
    }

    LaunchedEffect(Unit) {
        if (!isAuthenticated) {
            discoveryViewModel.handleIntent(DiscoveryIntent.Scan)
        }
    }

    AppBackHandler(enabled = !isAuthenticated && loginMode != AuthDestination.ServerSelection) {
        when (loginMode) {
            AuthDestination.ManualServerEntry -> loginMode = AuthDestination.ServerSelection
            AuthDestination.Home -> loginMode = AuthDestination.ServerSelection
            AuthDestination.QuickConnect -> {
                qcViewModel.handleIntent(QuickConnectIntent.Cancel)
                loginMode = AuthDestination.Home
            }
            AuthDestination.Manual -> loginMode = AuthDestination.Home
            else -> {}
        }
    }

    CompositionLocalProvider(LocalUiType provides uiType) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFFFFB300),
                surface = Color(0xFF121212),
                background = Color(0xFF121212),
                onSurface = Color.White,
                onBackground = Color.White,
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF121212),
            ) {
                if (isValidating) {
                    // Splash Screen / Loading
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("JellyPlex", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(32.dp))
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (isAuthenticated) {
                    MainScreen(mainViewModel = mainViewModel)
                } else {
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
                                            loginMode = AuthDestination.Home
                                        }
                                    )
                                },
                                onManualInput = { manualUrl ->
                                    discoveryViewModel.handleIntent(DiscoveryIntent.ClearError)
                                    if (manualUrl.isNotEmpty()) {
                                        discoveryViewModel.handleIntent(
                                            DiscoveryIntent.ValidateAndConnect(manualUrl) {
                                                sessionViewModel.updateServerUrl(manualUrl)
                                                loginMode = AuthDestination.Home
                                            }
                                        )
                                    } else {
                                        loginMode = AuthDestination.ManualServerEntry
                                    }
                                },
                                onTryDemo = {
                                    val demoUrl = "https://demo.jellyfin.org/stable"
                                    discoveryViewModel.handleIntent(
                                        DiscoveryIntent.ValidateAndConnect(demoUrl) {
                                            sessionViewModel.updateServerUrl(demoUrl)
                                            loginMode = AuthDestination.Home
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
                                            loginMode = AuthDestination.Home
                                        }
                                    )
                                },
                                onBack = {
                                    discoveryViewModel.handleIntent(DiscoveryIntent.ClearError)
                                    loginMode = AuthDestination.ServerSelection
                                }
                            )
                        }

                        AuthDestination.Home -> {
                            AuthHomeScreen(
                                baseUrl = sessionViewModel.getBaseUrl(),
                                onQuickConnect = {
                                    loginMode = AuthDestination.QuickConnect
                                    qcViewModel.handleIntent(QuickConnectIntent.Initiate)
                                },
                                onManualLogin = { loginMode = AuthDestination.Manual },
                                onChangeServer = { loginMode = AuthDestination.ServerSelection },
                            )
                        }

                        AuthDestination.QuickConnect -> {
                            QuickConnectScreen(qcState) {
                                qcViewModel.handleIntent(QuickConnectIntent.Cancel)
                                loginMode = AuthDestination.Home
                            }
                        }

                        AuthDestination.Manual -> {
                            ManualLoginScreen(
                                state = loginState,
                                currentUrl = sessionViewModel.getBaseUrl(),
                                onLogin = { url, user, pass ->
                                    loginViewModel.handleIntent(LoginIntent.Login(url, user, pass))
                                },
                                onBack = { loginMode = AuthDestination.Home },
                            )
                        }
                    }
                }
            }
        }
    }
}
