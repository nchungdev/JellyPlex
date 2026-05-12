package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.Constants
import org.jellyplus.client.domain.usecases.ClearSessionUseCase
import org.jellyplus.client.domain.usecases.GetBaseUrlUseCase
import org.jellyplus.client.domain.usecases.GetIsAuthenticatedUseCase
import org.jellyplus.client.domain.usecases.UpdateBaseUrlUseCase
import org.jellyplus.client.domain.usecases.ValidateSessionUseCase

data class SessionState(
    val isAuthenticated: Boolean = false,
    val isValidating: Boolean = false,
    val persistDemo: Boolean = false
)

class SessionViewModel(
    private val getIsAuthenticatedUseCase: GetIsAuthenticatedUseCase,
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val updateBaseUrlUseCase: UpdateBaseUrlUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _isValidating = MutableStateFlow(true) // Khởi tạo là true để giữ LoadingScreen ngay từ đầu

    val uiState: StateFlow<SessionState> = combine(
        getIsAuthenticatedUseCase(),
        _isValidating
    ) { authenticated, validating ->
        SessionState(
            isAuthenticated = authenticated,
            isValidating = validating,
            persistDemo = clearSessionUseCase.getPersistDemo()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionState(isAuthenticated = false, isValidating = true)
    )

    fun getBaseUrl(): String = getBaseUrlUseCase()
    fun getUserName(): String? = clearSessionUseCase.getUserName()

    init {
        validateStartupSession()
    }

    private fun validateStartupSession() {
        viewModelScope.launch(dispatchers.main) {
            // Lấy trạng thái auth hiện tại từ local storage
            val isCurrentlyAuthenticated = getIsAuthenticatedUseCase().first()

            if (isCurrentlyAuthenticated) {
                // Nếu đã có session, tiến hành validate với server
                try {
                    val isValid = validateSessionUseCase()
                    if (!isValid) {
                        println("SessionViewModel: Session invalid. Clearing.")
                        clearSessionUseCase()
                    }
                } catch (e: Exception) {
                    println("SessionViewModel: Auth error detected. Clearing session.")
                    clearSessionUseCase()
                } finally {
                    _isValidating.value = false
                }
            } else {
                // Nếu không có session, tắt loading ngay để vào màn Auth
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

    fun togglePersistDemo(enabled: Boolean) {
        viewModelScope.launch {
            clearSessionUseCase.setPersistDemo(enabled)
            // If we are currently on demo and disabled persistence, it might have cleared session
            if (!enabled && getBaseUrl().contains(Constants.DEMO_SERVER_HOST)) {
                logout()
            }
        }
    }
}
