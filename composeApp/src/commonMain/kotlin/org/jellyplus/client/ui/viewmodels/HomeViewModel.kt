package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.usecases.*

data class HomeState(
    val featuredItems: List<MediaItem> = emptyList(),
    val resumeItems: List<MediaItem> = emptyList(),
    val recentlyAddedItems: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class HomeViewModel(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val refreshHomeContentUseCase: RefreshHomeContentUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        // SSOT: Observe local data flow
        viewModelScope.launch {
            getHomeContentUseCase().collect { content ->
                if (content != null) {
                    _state.value = _state.value.copy(
                        featuredItems = content.featuredItems,
                        resumeItems = content.resumeItems,
                        recentlyAddedItems = content.recentlyAddedItems,
                        baseUrl = getBaseUrlUseCase()
                    )
                }
            }
        }

        loadHomeContent()
    }

    fun loadHomeContent() {
        val userId = getUserIdUseCase() ?: ""
        if (!hasSessionUseCase() || userId.isEmpty()) return

        viewModelScope.launch(dispatchers.io) {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = refreshHomeContentUseCase(userId)

            result.onSuccess {
                _state.value = _state.value.copy(isLoading = false)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)

                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
