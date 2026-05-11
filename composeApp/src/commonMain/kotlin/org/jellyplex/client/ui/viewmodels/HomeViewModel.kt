package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.usecases.*

data class HomeState(
    val resumeItems: List<MediaItem> = emptyList(),
    val recentlyAddedItems: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class HomeViewModel(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getHomeCacheUseCase: GetHomeCacheUseCase,
    private val saveHomeCacheUseCase: SaveHomeCacheUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        if (!hasSessionUseCase()) return
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Try to load from cache first
            val cachedContent = getHomeCacheUseCase()
            if (cachedContent != null) {
                _state.value = _state.value.copy(
                    resumeItems = cachedContent.resumeItems,
                    recentlyAddedItems = cachedContent.recentlyAddedItems,
                    baseUrl = getBaseUrlUseCase()
                )
            }

            // 2. Fetch from API
            _state.value = _state.value.copy(isLoading = true, error = null)
            val userId = getUserIdUseCase() ?: ""
            val result = getHomeContentUseCase(userId)

            result.onSuccess { content ->
                _state.value =
                    _state.value.copy(
                        resumeItems = content.resumeItems,
                        recentlyAddedItems = content.recentlyAddedItems,
                        baseUrl = getBaseUrlUseCase(),
                        isLoading = false,
                    )
                // Save to cache
                saveHomeCacheUseCase(content)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)

                // If it's an authentication error, clear session
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                    // The App.kt should observe session change or we can have a side effect here
                }
            }
        }
    }
}
