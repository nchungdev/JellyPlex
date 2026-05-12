package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.usecases.*

data class SessionState(
    val isAuthenticated: Boolean = false,
    val isValidating: Boolean = false
)

class SessionViewModel(
    private val getIsAuthenticatedUseCase: GetIsAuthenticatedUseCase,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val updateBaseUrlUseCase: UpdateBaseUrlUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _isValidating = MutableStateFlow(false)
    
    val uiState: StateFlow<SessionState> = combine(
        getIsAuthenticatedUseCase(),
        _isValidating
    ) { authenticated, validating ->
        SessionState(authenticated, validating)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionState(isAuthenticated = false, isValidating = true)
    )

    fun getBaseUrl(): String = getBaseUrlUseCase()

    init {
        validateStartupSession()
    }

    private fun validateStartupSession() {
        viewModelScope.launch(dispatchers.main) {
            val isCurrentlyAuthenticated = getIsAuthenticatedUseCase().first()
            if (isCurrentlyAuthenticated) {
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
        viewModelScope.launch(dispatchers.main) {
            clearSessionUseCase()
        }
    }

    fun updateServerUrl(url: String) {
        updateBaseUrlUseCase(url)
    }
}
