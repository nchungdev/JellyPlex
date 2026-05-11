package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jellyplex.client.domain.usecases.*

data class SessionState(
    val isAuthenticated: Boolean = false,
    val isValidating: Boolean = false
)

class SessionViewModel(
    private val getIsAuthenticatedUseCase: GetIsAuthenticatedUseCase,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val updateBaseUrlUseCase: UpdateBaseUrlUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase
) : ViewModel() {

    private val _isValidating = MutableStateFlow(getIsAuthenticatedUseCase().value)
    
    val uiState: StateFlow<SessionState> = combine(
        getIsAuthenticatedUseCase(),
        _isValidating
    ) { authenticated, validating ->
        SessionState(authenticated, validating)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionState(
            isAuthenticated = getIsAuthenticatedUseCase().value,
            isValidating = getIsAuthenticatedUseCase().value
        )
    )

    fun getBaseUrl(): String = getBaseUrlUseCase()

    init {
        validateStartupSession()
    }

    private fun validateStartupSession() {
        viewModelScope.launch {
            if (getIsAuthenticatedUseCase().value) {
                _isValidating.value = true
                try {
                    val isValid = validateSessionUseCase()
                    if (!isValid) {
                        println("SessionViewModel: Session invalid. Clearing.")
                        clearSessionUseCase()
                    } else {
                        println("SessionViewModel: Validation success or network error (keeping session).")
                    }
                } catch (e: Exception) {
                    println("SessionViewModel: Auth error detected. Clearing session.")
                    clearSessionUseCase()
                } finally {
                    _isValidating.value = false
                }
            } else {
                _isValidating.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            clearSessionUseCase()
        }
    }

    fun updateServerUrl(url: String) {
        updateBaseUrlUseCase(url)
    }
}
