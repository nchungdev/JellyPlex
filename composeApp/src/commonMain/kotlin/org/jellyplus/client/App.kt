package org.jellyplus.client

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jellyplus.client.di.appModule
import org.jellyplus.client.ui.viewmodels.DiscoveryIntent
import org.jellyplus.client.ui.viewmodels.DiscoveryViewModel
import org.jellyplus.client.ui.viewmodels.LoginViewModel
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.QuickConnectIntent
import org.jellyplus.client.ui.viewmodels.QuickConnectViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule())
    }) {
        AppContent()
    }
}

@Composable
fun AppContent() {
    val uiType = getUiType()

    if (uiType == UiType.Mobile) {
        OrientationEffect(ScreenOrientation.Portrait)
    }

    val sessionViewModel = koinViewModel<SessionViewModel>()
    val qcViewModel = koinViewModel<QuickConnectViewModel>()
    val loginViewModel = koinViewModel<LoginViewModel>()
    val discoveryViewModel = koinViewModel<DiscoveryViewModel>()
    val mainViewModel = koinViewModel<MainViewModel>()

    val sessionState by sessionViewModel.uiState.collectAsState()
    val isAuthenticated = sessionState.isAuthenticated
    val isValidating = sessionState.isValidating

    val qcState by qcViewModel.state.collectAsState()
    val loginState by loginViewModel.state.collectAsState()
    val discoveryState by discoveryViewModel.state.collectAsState()

    var loginMode by remember { mutableStateOf(AuthDestination.ServerSelection) }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            loginMode = AuthDestination.Home
            mainViewModel.loadData()
        } else {
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
                AppMainContent(
                    isValidating = isValidating,
                    isAuthenticated = isAuthenticated,
                    loginMode = loginMode,
                    mainViewModel = mainViewModel,
                    sessionViewModel = sessionViewModel,
                    qcViewModel = qcViewModel,
                    discoveryViewModel = discoveryViewModel,
                    loginViewModel = loginViewModel,
                    discoveryState = discoveryState,
                    qcState = qcState,
                    loginState = loginState,
                    onLoginModeChange = { loginMode = it }
                )
            }
        }
    }
}
