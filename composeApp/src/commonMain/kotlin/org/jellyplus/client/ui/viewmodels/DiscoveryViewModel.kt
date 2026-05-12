package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.discovery.DiscoveredServer
import org.jellyplus.client.domain.usecases.DiscoverServersUseCase
import org.jellyplus.client.domain.usecases.ValidateServerUseCase

data class DiscoveryState(
    val isScanning: Boolean = false,
    val discoveredServers: List<DiscoveredServer> = emptyList(),
    val error: String? = null,
    val isValidatingServer: Boolean = false,
)

sealed class DiscoveryIntent {
    object Scan : DiscoveryIntent()

    object CancelScan : DiscoveryIntent()

    object ClearError : DiscoveryIntent()

    data class ValidateAndConnect(val url: String, val onSuccess: () -> Unit) : DiscoveryIntent()
}

class DiscoveryViewModel(
    private val discoverServersUseCase: DiscoverServersUseCase,
    private val validateServerUseCase: ValidateServerUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoveryState())
    val state: StateFlow<DiscoveryState> = _state.asStateFlow()
    private var scanJob: Job? = null

    fun handleIntent(intent: DiscoveryIntent) {
        when (intent) {
            is DiscoveryIntent.Scan -> scan()
            is DiscoveryIntent.CancelScan -> cancelScan()
            is DiscoveryIntent.ClearError -> _state.value = _state.value.copy(error = null)
            is DiscoveryIntent.ValidateAndConnect -> validateServer(intent.url, intent.onSuccess)
        }
    }

    private fun validateServer(url: String, onSuccess: () -> Unit) {
        // ViewModel stays on Main thread for UI safety
        viewModelScope.launch {
            _state.value = _state.value.copy(isValidatingServer = true, error = null)
            try {
                // Repo handles the IO switch internally
                val isValid = validateServerUseCase(url)
                if (isValid) {
                    onSuccess()
                } else {
                    _state.value = _state.value.copy(error = "Could not connect to server: $url")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Could not connect to server: $url")
            } finally {
                _state.value = _state.value.copy(isValidatingServer = false)
            }
        }
    }

    private fun scan() {
        scanJob?.cancel()
        scanJob =
            viewModelScope.launch {
                val startTime: Long = Clock.System.now().toEpochMilliseconds()
                _state.value = _state.value.copy(isScanning = true, error = null, discoveredServers = emptyList())
                try {
                    discoverServersUseCase().collect { servers ->
                        _state.value = _state.value.copy(discoveredServers = servers)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _state.value = _state.value.copy(error = "Could not scan network")
                } finally {
                    // Ensure a minimum scanning time of 1500ms to prevent UI flicker
                    val currentTime: Long = Clock.System.now().toEpochMilliseconds()
                    val elapsedTime: Long = currentTime - startTime
                    val remainingDelay: Long = 1500L - elapsedTime
                    if (remainingDelay > 0L) {
                        delay(remainingDelay)
                    }
                    _state.value = _state.value.copy(isScanning = false)
                }
            }
    }

    private fun cancelScan() {
        scanJob?.cancel()
        _state.value = _state.value.copy(isScanning = false)
    }
}
