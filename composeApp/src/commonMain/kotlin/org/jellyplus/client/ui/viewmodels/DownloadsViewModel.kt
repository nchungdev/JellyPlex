package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jellyplus.client.domain.models.MediaItem

data class DownloadTask(
    val mediaItem: MediaItem,
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val isPaused: Boolean = false,
)

data class DownloadsState(
    val tasks: List<DownloadTask> = emptyList(),
)


class DownloadsViewModel : ViewModel() {
    private val _state = MutableStateFlow(DownloadsState())
    val state: StateFlow<DownloadsState> = _state.asStateFlow()

    fun startDownload(item: MediaItem) {
        val newTask = DownloadTask(mediaItem = item)
        _state.value = _state.value.copy(tasks = _state.value.tasks + newTask)
        // Implementation for actual downloading would go here
    }

    fun removeDownload(itemId: String) {
        _state.value =
            _state.value.copy(
                tasks = _state.value.tasks.filter { it.mediaItem.id != itemId },
            )
    }
}
