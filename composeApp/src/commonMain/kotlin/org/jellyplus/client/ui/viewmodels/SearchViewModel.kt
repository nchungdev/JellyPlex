package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.usecases.GetBaseUrlUseCase
import org.jellyplus.client.domain.usecases.SearchItemsUseCase

data class SearchState(
    val query: String = "",
    val results: List<MediaItem> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SearchViewModel(
    private val searchItemsUseCase: SearchItemsUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val settings: Settings,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    companion object {
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val HISTORY_SEPARATOR = "\u001F"
        private const val MAX_HISTORY_ITEMS = 10
    }

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        _state.value = _state.value.copy(
            searchHistory = settings.getStringOrNull(KEY_SEARCH_HISTORY)
                ?.split(HISTORY_SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
            baseUrl = getBaseUrlUseCase(),
        )
    }

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)

        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), isLoading = false)
            return
        }

        searchJob =
            viewModelScope.launch(dispatchers.io) {
                delay(500) // Debounce
                _state.value = _state.value.copy(isLoading = true)
                val result = searchItemsUseCase(newQuery)

                result.onSuccess { results ->
                    rememberQuery(newQuery)
                    _state.value =
                        _state.value.copy(
                            results = results,
                            baseUrl = getBaseUrlUseCase(),
                            isLoading = false,
                        )
                }.onFailure { e ->
                    _state.value = _state.value.copy(error = e.message, isLoading = false)
                }
            }
    }

    fun clearQuery() {
        searchJob?.cancel()
        _state.value = _state.value.copy(query = "", results = emptyList(), isLoading = false, error = null)
    }

    fun clearHistory() {
        settings.remove(KEY_SEARCH_HISTORY)
        _state.value = _state.value.copy(searchHistory = emptyList())
    }

    private fun rememberQuery(query: String) {
        val normalized = query.trim()
        if (normalized.isBlank()) return

        val nextHistory = listOf(normalized) +
            _state.value.searchHistory.filterNot { it.equals(normalized, ignoreCase = true) }
        val capped = nextHistory.take(MAX_HISTORY_ITEMS)
        settings.putString(KEY_SEARCH_HISTORY, capped.joinToString(HISTORY_SEPARATOR))
        _state.value = _state.value.copy(searchHistory = capped)
    }
}
