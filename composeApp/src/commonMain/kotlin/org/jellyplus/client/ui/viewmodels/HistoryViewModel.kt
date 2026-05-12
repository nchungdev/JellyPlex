package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.usecases.*

data class HistoryState(
    val watchedItems: List<MediaItem> = emptyList(),
    val resumeItems: List<MediaItem> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class HistoryViewModel(
    private val getWatchHistoryUseCase: GetWatchHistoryUseCase,
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val refreshHomeContentUseCase: RefreshHomeContentUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val hasSessionUseCase: HasSessionUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getHomeContentUseCase().collect { content ->
                if (content != null) {
                    _state.value = _state.value.copy(resumeItems = content.resumeItems)
                }
            }
        }
        loadHistory()
    }

    fun loadHistory() {
        val userId = getUserIdUseCase() ?: return
        if (!hasSessionUseCase()) return

        viewModelScope.launch(dispatchers.io) {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val resumeDeferred = async { runCatching { refreshHomeContentUseCase(userId) } }
            val historyDeferred = async { runCatching { getWatchHistoryUseCase(userId) } }

            resumeDeferred.await()
            val historyResult = historyDeferred.await()

            historyResult.onSuccess { items ->
                _state.value = _state.value.copy(
                    watchedItems = items,
                    baseUrl = getBaseUrlUseCase(),
                    isLoading = false,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)
                if (e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true) {
                    clearSessionUseCase()
                }
            }
        }
    }
}
