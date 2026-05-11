package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplex.client.domain.models.AppDispatchers
import org.jellyplex.client.domain.usecases.InitiateQuickConnectUseCase
import org.jellyplex.client.domain.usecases.PollQuickConnectStatusUseCase

data class QuickConnectState(
    val isLoading: Boolean = false,
    val code: String? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val accessToken: String? = null,
    val userId: String? = null,
)

sealed class QuickConnectIntent {
    object Initiate : QuickConnectIntent()
    object Cancel : QuickConnectIntent()
}

class QuickConnectViewModel(
    private val initiateQuickConnectUseCase: InitiateQuickConnectUseCase,
    private val pollQuickConnectStatusUseCase: PollQuickConnectStatusUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(QuickConnectState())
    val state: StateFlow<QuickConnectState> = _state.asStateFlow()

    private var pollingJob: Job? = null

    fun handleIntent(intent: QuickConnectIntent) {
        when (intent) {
            is QuickConnectIntent.Initiate -> initiate()
            is QuickConnectIntent.Cancel -> cancelPolling()
        }
    }

    private fun initiate() {
        cancelPolling()
        pollingJob = viewModelScope.launch(dispatchers.io) {
            _state.value = _state.value.copy(isLoading = true, error = null, isAuthenticated = false)

            val result = initiateQuickConnectUseCase()

            result.onSuccess { quickConnectResult ->
                _state.value = _state.value.copy(isLoading = false, code = quickConnectResult.code)

                quickConnectResult.secret?.let { secret ->
                    pollQuickConnectStatusUseCase(secret).collect { status ->
                        // Fix: Only mark as authenticated when we actually have a token
                        if (status.authenticated && status.authenticationToken != null) {
                            println("QuickConnectViewModel: Authentication confirmed with token.")
                            viewModelScope.launch(dispatchers.main) {
                                _state.value =
                                    _state.value.copy(
                                        isAuthenticated = true,
                                        accessToken = status.authenticationToken,
                                        userId = status.userId,
                                    )
                                cancelPolling() // Stop once confirmed
                            }
                        } else if (status.authenticated) {
                            println("QuickConnectViewModel: User authorized, waiting for token...")
                        }
                    }
                }
            }.onFailure { e ->
                val errorMsg =
                    if (e.message?.contains("NoTransformationFoundException") == true) {
                        "Invalid server response. Ensure you are connecting to a Jellyfin server, " +
                            "not a web portal or proxy."
                    } else {
                        e.message ?: "Unknown error occurred"
                    }
                _state.value = _state.value.copy(isLoading = false, error = errorMsg)
            }
        }
    }

    private fun cancelPolling() {
        println("QuickConnectViewModel: Cancelling polling job.")
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelPolling()
    }
}
