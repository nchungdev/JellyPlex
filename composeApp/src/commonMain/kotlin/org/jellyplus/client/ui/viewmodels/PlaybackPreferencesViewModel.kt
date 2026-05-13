package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

data class PlaybackPreferencesState(
    val autoSkipIntro: Boolean = false,
    val autoSkipOutro: Boolean = false,
    val autoSkipRecap: Boolean = false,
    val autoSkipPreview: Boolean = false,
    val autoNext: Boolean = true,
    val autoPictureInPicture: Boolean = true,
    val seamlessTransition: Boolean = false,
    val preferOriginalAudio: Boolean = true,
    val showGestureHints: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val seekBackSeconds: Int = 5,
    val seekForwardSeconds: Int = 10,
)

class PlaybackPreferencesViewModel(
    private val settings: PlayerSettingsLocalDataSource,
) : ViewModel() {
    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<PlaybackPreferencesState> = _state.asStateFlow()

    fun setAutoSkipIntro(enabled: Boolean) = update { settings.autoSkipIntro = enabled }
    fun setAutoSkipOutro(enabled: Boolean) = update { settings.autoSkipOutro = enabled }
    fun setAutoSkipRecap(enabled: Boolean) = update { settings.autoSkipRecap = enabled }
    fun setAutoSkipPreview(enabled: Boolean) = update { settings.autoSkipPreview = enabled }
    fun setAutoNext(enabled: Boolean) = update { settings.autoNext = enabled }
    fun setAutoPictureInPicture(enabled: Boolean) = update { settings.autoPictureInPicture = enabled }
    fun setSeamlessTransition(enabled: Boolean) = update { settings.seamlessTransition = enabled }
    fun setPreferOriginalAudio(enabled: Boolean) = update { settings.preferOriginalAudio = enabled }
    fun setShowGestureHints(enabled: Boolean) = update { settings.showGestureHints = enabled }
    fun setPlaybackSpeed(speed: Float) = update { settings.playbackSpeed = speed }
    fun setSeekBackSeconds(seconds: Int) = update { settings.seekBackSeconds = seconds }
    fun setSeekForwardSeconds(seconds: Int) = update { settings.seekForwardSeconds = seconds }

    private fun update(write: () -> Unit) {
        write()
        _state.value = loadState()
    }

    private fun loadState(): PlaybackPreferencesState =
        PlaybackPreferencesState(
            autoSkipIntro = settings.autoSkipIntro,
            autoSkipOutro = settings.autoSkipOutro,
            autoSkipRecap = settings.autoSkipRecap,
            autoSkipPreview = settings.autoSkipPreview,
            autoNext = settings.autoNext,
            autoPictureInPicture = settings.autoPictureInPicture,
            seamlessTransition = settings.seamlessTransition,
            preferOriginalAudio = settings.preferOriginalAudio,
            showGestureHints = settings.showGestureHints,
            playbackSpeed = settings.playbackSpeed,
            seekBackSeconds = settings.seekBackSeconds,
            seekForwardSeconds = settings.seekForwardSeconds,
        )
}
