package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplex.client.domain.discovery.DiscoveredServer
import org.jellyplex.client.domain.usecases.DiscoverServersUseCase

data class DiscoveryState(
    val isScanning: Boolean = false,
    val discoveredServers: List<DiscoveredServer> = emptyList(),
    val error: String? = null,
)

sealed class DiscoveryIntent {
    object Scan : DiscoveryIntent()

    object CancelScan : DiscoveryIntent()
}

class DiscoveryViewModel(
    private val discoverServersUseCase: DiscoverServersUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoveryState())
    val state: StateFlow<DiscoveryState> = _state.asStateFlow()
    private var scanJob: Job? = null

    init {
        scan()
    }

    fun handleIntent(intent: DiscoveryIntent) {
        when (intent) {
            is DiscoveryIntent.Scan -> scan()
            is DiscoveryIntent.CancelScan -> cancelScan()
        }
    }

    private fun scan() {
        scanJob?.cancel()
        scanJob =
            viewModelScope.launch(Dispatchers.IO) {
                val startTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                _state.value = _state.value.copy(isScanning = true, error = null, discoveredServers = emptyList())
                try {
                    discoverServersUseCase().collect { servers ->
                        _state.value = _state.value.copy(discoveredServers = servers)
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(error = e.message)
                } finally {
                    // Ensure a minimum scanning time of 1500ms to prevent UI flicker
                    val elapsedTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - startTime
                    val remainingDelay = 1500L - elapsedTime
                    if (remainingDelay > 0) {
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
