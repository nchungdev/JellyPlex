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
import org.jellyplus.client.domain.usecases.GetAccessTokenUseCase
import org.jellyplus.client.domain.usecases.GetUserIdUseCase
import org.jellyplus.client.domain.usecases.ReportPlaybackProgressUseCase
import org.jellyplus.client.domain.usecases.ReportPlaybackStartUseCase
import org.jellyplus.client.domain.usecases.ReportPlaybackStoppedUseCase
import org.jellyplus.client.domain.usecases.ResolveStreamConfigUseCase

data class PlayerState(
    val url: String? = null,
    val mimeType: String? = null,
    val accessToken: String = "",
    val playSessionId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PlayerViewModel(
    private val resolveStreamConfigUseCase: ResolveStreamConfigUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val getAccessTokenUseCase: GetAccessTokenUseCase,
    private val reportPlaybackStartUseCase: ReportPlaybackStartUseCase,
    private val reportPlaybackProgressUseCase: ReportPlaybackProgressUseCase,
    private val reportPlaybackStoppedUseCase: ReportPlaybackStoppedUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    fun loadStreamUrl(item: MediaItem) {
        if (_state.value.url != null) return

        viewModelScope.launch(dispatchers.main) {
            _state.value = _state.value.copy(isLoading = true, accessToken = getAccessTokenUseCase() ?: "")
            val userId = getUserIdUseCase() ?: ""
            val deviceId = "CMP-ID"

            try {
                val config = resolveStreamConfigUseCase(item, userId, deviceId)
                if (config != null) {
                    _state.value = _state.value.copy(
                        url = config.url,
                        playSessionId = config.playSessionId,
                        mimeType = config.mimeType,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "Failed to resolve stream configuration",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun reportStart(itemId: String, playSessionId: String) {
        viewModelScope.launch(dispatchers.io) {
            try {
                reportPlaybackStartUseCase(itemId, playSessionId)
            } catch (_: Exception) {
            }
        }
    }

    fun reportProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean = false
    ) {
        viewModelScope.launch(dispatchers.io) {
            try {
                reportPlaybackProgressUseCase(itemId, playSessionId, positionTicks, isPaused, isMuted)
            } catch (_: Exception) {
            }
        }
    }

    fun reportStopped(itemId: String, playSessionId: String, positionTicks: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                reportPlaybackStoppedUseCase(itemId, playSessionId, positionTicks)
            } catch (_: Exception) {
            }
        }
    }
}
