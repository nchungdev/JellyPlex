package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplex.client.domain.models.AppDispatchers
import org.jellyplex.client.domain.usecases.LoginUseCase

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val username: String? = null,
    val accessToken: String? = null,
    val userId: String? = null,
)

sealed class LoginIntent {
    data class Login(val url: String, val username: String, val password: String) : LoginIntent()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val dispatchers: AppDispatchers, // Still keeping it for testability if needed elsewhere
) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Login -> login(intent.url, intent.username, intent.password)
        }
    }

    private fun login(
        url: String,
        username: String,
        password: String,
    ) {
        // ViewModel launches on Main (default)
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // loginUseCase/Repo will handle the IO thread switch
            val result = loginUseCase(url, username, password)

            result.onSuccess { authResult ->
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        username = authResult.user?.name,
                        accessToken = authResult.accessToken,
                        userId = authResult.user?.id,
                    )
            }.onFailure { e ->
                val errorMsg =
                    if (e.message?.contains("NoTransformationFoundException") == true) {
                        "Invalid server response. Ensure the URL is correct and points to a Jellyfin server."
                    } else {
                        e.message ?: "Unknown error occurred"
                    }
                _state.value = _state.value.copy(isLoading = false, error = errorMsg)
            }
        }
    }
}
