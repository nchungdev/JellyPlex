package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.domain.usecases.GetBaseUrlUseCase
import org.jellyplus.client.domain.usecases.SearchItemsUseCase

data class SearchState(
    val query: String = "",
    val results: List<MediaItem> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    /** null = All types */
    val selectedFilter: MediaType? = null,
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    /** Results filtered by the active type chip. */
    val displayResults: List<MediaItem>
        get() = if (selectedFilter == null) results
                else results.filter { it.type == selectedFilter }
}

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
        _state.value = _state.value.copy(query = newQuery, error = null)

        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), isLoading = false)
            return
        }

        searchJob = viewModelScope.launch(dispatchers.io) {
            delay(400) // debounce
            _state.value = _state.value.copy(isLoading = true)
            val result = searchItemsUseCase(newQuery)

            result.onSuccess { items ->
                rememberQuery(newQuery)
                _state.value = _state.value.copy(
                    results = items,
                    baseUrl = getBaseUrlUseCase(),
                    isLoading = false,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun onFilterChange(type: MediaType?) {
        _state.value = _state.value.copy(selectedFilter = type)
    }

    fun clearQuery() {
        searchJob?.cancel()
        _state.value = _state.value.copy(
            query = "", results = emptyList(),
            isLoading = false, error = null,
        )
    }

    fun clearHistory() {
        settings.remove(KEY_SEARCH_HISTORY)
        _state.value = _state.value.copy(searchHistory = emptyList())
    }

    fun removeHistoryItem(query: String) {
        val next = _state.value.searchHistory.filter { it != query }
        settings.putString(KEY_SEARCH_HISTORY, next.joinToString(HISTORY_SEPARATOR))
        _state.value = _state.value.copy(searchHistory = next)
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
