package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SearchViewModel(
    private val searchItemsUseCase: SearchItemsUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

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
}
