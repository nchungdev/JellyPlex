package org.jellyplex.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.models.Person
import org.jellyplex.client.domain.usecases.GetBaseUrlUseCase
import org.jellyplex.client.domain.usecases.GetItemDetailsUseCase
import org.jellyplex.client.domain.usecases.GetPeopleUseCase
import org.jellyplex.client.domain.usecases.GetUserIdUseCase

data class MovieDetailState(
    val itemId: String? = null,
    val fullItem: MediaItem? = null,
    val cast: List<Person> = emptyList(),
    val baseUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class MovieDetailViewModel(
    private val getItemDetailsUseCase: GetItemDetailsUseCase,
    private val getPeopleUseCase: GetPeopleUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getBaseUrlUseCase: GetBaseUrlUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieDetailState())
    val state: StateFlow<MovieDetailState> = _state.asStateFlow()

    fun loadMovieDetails(item: MediaItem) {
        if (_state.value.itemId == item.id && _state.value.fullItem != null) return

        viewModelScope.launch {
            _state.value = MovieDetailState(
                itemId = item.id,
                fullItem = item,
                isLoading = true,
                baseUrl = getBaseUrlUseCase()
            )

            val userId = getUserIdUseCase() ?: ""
            val itemDetailsResult = getItemDetailsUseCase(item.id, userId)
            val peopleResult = getPeopleUseCase(item.id)

            if (itemDetailsResult.isSuccess && peopleResult.isSuccess) {
                _state.value = _state.value.copy(
                    fullItem = itemDetailsResult.getOrNull(),
                    cast = peopleResult.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                val error = itemDetailsResult.exceptionOrNull()?.message ?: peopleResult.exceptionOrNull()?.message
                _state.value = _state.value.copy(isLoading = false, error = error)
            }
        }
    }
}
