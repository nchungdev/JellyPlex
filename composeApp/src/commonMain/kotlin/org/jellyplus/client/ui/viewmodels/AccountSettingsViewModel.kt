package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.usecases.AuthorizeQuickConnectUseCase
import org.jellyplus.client.domain.usecases.ChangePasswordUseCase

data class AccountSettingsState(
    val isChangingPassword: Boolean = false,
    val passwordMessage: String? = null,
    val isAuthorizingQuickConnect: Boolean = false,
    val quickConnectMessage: String? = null,
)

class AccountSettingsViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val authorizeQuickConnectUseCase: AuthorizeQuickConnectUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(AccountSettingsState())
    val state: StateFlow<AccountSettingsState> = _state.asStateFlow()

    fun clearMessages() {
        _state.value = _state.value.copy(passwordMessage = null, quickConnectMessage = null)
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        when {
            currentPassword.isBlank() -> {
                _state.value = _state.value.copy(passwordMessage = "Enter your current password")
                return
            }
            newPassword.length < 4 -> {
                _state.value = _state.value.copy(passwordMessage = "New password is too short")
                return
            }
            newPassword != confirmPassword -> {
                _state.value = _state.value.copy(passwordMessage = "Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isChangingPassword = true, passwordMessage = null)
            try {
                changePasswordUseCase(currentPassword, newPassword)
                _state.value = _state.value.copy(
                    isChangingPassword = false,
                    passwordMessage = "Password updated",
                )
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    isChangingPassword = false,
                    passwordMessage = e.message ?: "Could not update password",
                )
            }
        }
    }

    fun authorizeQuickConnect(code: String) {
        val normalizedCode = code.trim().replace(" ", "")
        if (normalizedCode.isBlank()) {
            _state.value = _state.value.copy(quickConnectMessage = "Enter a Quick Connect code")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isAuthorizingQuickConnect = true, quickConnectMessage = null)
            try {
                val authorized = authorizeQuickConnectUseCase(normalizedCode)
                _state.value = _state.value.copy(
                    isAuthorizingQuickConnect = false,
                    quickConnectMessage = if (authorized) "Device authorized" else "Code was not accepted",
                )
            } catch (e: Throwable) {
                _state.value = _state.value.copy(
                    isAuthorizingQuickConnect = false,
                    quickConnectMessage = e.message ?: "Could not authorize device",
                )
            }
        }
    }
}
