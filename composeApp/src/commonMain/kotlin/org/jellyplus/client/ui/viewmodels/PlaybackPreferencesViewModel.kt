package org.jellyplus.client.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jellyplus.client.domain.usecases.PlayerPreferencesUseCases

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
    val doubleTapSeekEnabled: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val seekBackSeconds: Int = 5,
    val seekForwardSeconds: Int = 10,
    val holdSpeedEnabled: Boolean = true,
    val holdSpeedMultiplier: String = "2x",
    val swipeLeftBrightnessEnabled: Boolean = true,
    val swipeRightVolumeEnabled: Boolean = true,
    val maxAudioChannels: String = "Auto",
    val preferredAudioLanguage: String = "Any",
    val playDefaultAudioTrack: Boolean = false,
    val internetVideoQuality: String = "Auto",
    val maxTranscodeResolution: String = "Auto",
    val limitMaxVideoResolution: Boolean = false,
    val musicInternetQuality: String = "Auto",
    val rememberAudioTrack: Boolean = true,
    val rememberSubtitleTrack: Boolean = true,
    val showNextVideoInfo: Boolean = true,
    val audioNormalization: String = "Off",
    val subtitleLanguage: String = "Any",
    val subtitleMode: String = "Default",
    val subtitleTextSize: String = "Normal",
    val subtitleTextWeight: String = "Normal",
    val subtitleFontStyle: String = "Default",
    val subtitleTextColor: String = "White",
    val subtitleBackgroundColor: String = "Black",
    val subtitleShadow: String = "Drop shadow",
    val subtitleVerticalPosition: String = "Bottom",
    val appLanguage: String = "System",
    val appTheme: String = "Dark",
    val posterDensity: String = "Comfortable",
    val imageQuality: String = "Auto",
    val metadataDisplay: String = "Title and progress",
    val homeSectionOrder: String = "hero,continue,recent,genreRows,movies,tv,genres",
    val homeEnabledSections: String = "hero,continue,recent,genreRows,movies,tv,genres",
)

class PlaybackPreferencesViewModel(
    private val settings: PlayerPreferencesUseCases,
) : ViewModel() {
    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<PlaybackPreferencesState> = _state.asStateFlow()

    fun setAutoSkipIntro(enabled: Boolean) = update { settings.setAutoSkipIntro(enabled) }
    fun setAutoSkipOutro(enabled: Boolean) = update { settings.setAutoSkipOutro(enabled) }
    fun setAutoSkipRecap(enabled: Boolean) = update { settings.setAutoSkipRecap(enabled) }
    fun setAutoSkipPreview(enabled: Boolean) = update { settings.setAutoSkipPreview(enabled) }
    fun setAutoNext(enabled: Boolean) = update { settings.setAutoNext(enabled) }
    fun setAutoPictureInPicture(enabled: Boolean) = update { settings.setAutoPictureInPicture(enabled) }
    fun setSeamlessTransition(enabled: Boolean) = update { settings.setSeamlessTransition(enabled) }
    fun setPreferOriginalAudio(enabled: Boolean) = update { settings.setPreferOriginalAudio(enabled) }
    fun setShowGestureHints(enabled: Boolean) = update { settings.setShowGestureHints(enabled) }
    fun setDoubleTapSeekEnabled(enabled: Boolean) = update { settings.setDoubleTapSeekEnabled(enabled) }
    fun setPlaybackSpeed(speed: Float) = update { settings.setPlaybackSpeed(speed) }
    fun setSeekBackSeconds(seconds: Int) = update { settings.setSeekBackSeconds(seconds) }
    fun setSeekForwardSeconds(seconds: Int) = update { settings.setSeekForwardSeconds(seconds) }
    fun setHoldSpeedEnabled(enabled: Boolean) = update { settings.setHoldSpeedEnabled(enabled) }
    fun setHoldSpeedMultiplier(value: String) = update { settings.setHoldSpeedMultiplier(value) }
    fun setSwipeLeftBrightnessEnabled(enabled: Boolean) = update { settings.setSwipeLeftBrightnessEnabled(enabled) }
    fun setSwipeRightVolumeEnabled(enabled: Boolean) = update { settings.setSwipeRightVolumeEnabled(enabled) }
    fun setMaxAudioChannels(value: String) = update { settings.setMaxAudioChannels(value) }
    fun setPreferredAudioLanguage(value: String) = update { settings.setPreferredAudioLanguage(value) }
    fun setPlayDefaultAudioTrack(enabled: Boolean) = update { settings.setPlayDefaultAudioTrack(enabled) }
    fun setInternetVideoQuality(value: String) = update { settings.setInternetVideoQuality(value) }
    fun setMaxTranscodeResolution(value: String) = update { settings.setMaxTranscodeResolution(value) }
    fun setLimitMaxVideoResolution(enabled: Boolean) = update { settings.setLimitMaxVideoResolution(enabled) }
    fun setMusicInternetQuality(value: String) = update { settings.setMusicInternetQuality(value) }
    fun setRememberAudioTrack(enabled: Boolean) = update { settings.setRememberAudioTrack(enabled) }
    fun setRememberSubtitleTrack(enabled: Boolean) = update { settings.setRememberSubtitleTrack(enabled) }
    fun setShowNextVideoInfo(enabled: Boolean) = update { settings.setShowNextVideoInfo(enabled) }
    fun setAudioNormalization(value: String) = update { settings.setAudioNormalization(value) }
    fun setSubtitleLanguage(value: String) = update { settings.setSubtitleLanguage(value) }
    fun setSubtitleMode(value: String) = update { settings.setSubtitleMode(value) }
    fun setSubtitleTextSize(value: String) = update { settings.setSubtitleTextSize(value) }
    fun setSubtitleTextWeight(value: String) = update { settings.setSubtitleTextWeight(value) }
    fun setSubtitleFontStyle(value: String) = update { settings.setSubtitleFontStyle(value) }
    fun setSubtitleTextColor(value: String) = update { settings.setSubtitleTextColor(value) }
    fun setSubtitleBackgroundColor(value: String) = update { settings.setSubtitleBackgroundColor(value) }
    fun setSubtitleShadow(value: String) = update { settings.setSubtitleShadow(value) }
    fun setSubtitleVerticalPosition(value: String) = update { settings.setSubtitleVerticalPosition(value) }
    fun setAppLanguage(value: String) = update { settings.setAppLanguage(value) }
    fun setAppTheme(value: String) = update { settings.setAppTheme(value) }
    fun setPosterDensity(value: String) = update { settings.setPosterDensity(value) }
    fun setImageQuality(value: String) = update { settings.setImageQuality(value) }
    fun setMetadataDisplay(value: String) = update { settings.setMetadataDisplay(value) }
    fun setHomeSectionOrder(value: String) = update { settings.setHomeSectionOrder(value) }
    fun setHomeEnabledSections(value: String) = update { settings.setHomeEnabledSections(value) }

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
            doubleTapSeekEnabled = settings.doubleTapSeekEnabled,
            playbackSpeed = settings.playbackSpeed,
            seekBackSeconds = settings.seekBackSeconds,
            seekForwardSeconds = settings.seekForwardSeconds,
            holdSpeedEnabled = settings.holdSpeedEnabled,
            holdSpeedMultiplier = settings.holdSpeedMultiplier,
            swipeLeftBrightnessEnabled = settings.swipeLeftBrightnessEnabled,
            swipeRightVolumeEnabled = settings.swipeRightVolumeEnabled,
            maxAudioChannels = settings.maxAudioChannels,
            preferredAudioLanguage = settings.preferredAudioLanguage,
            playDefaultAudioTrack = settings.playDefaultAudioTrack,
            internetVideoQuality = settings.internetVideoQuality,
            maxTranscodeResolution = settings.maxTranscodeResolution,
            limitMaxVideoResolution = settings.limitMaxVideoResolution,
            musicInternetQuality = settings.musicInternetQuality,
            rememberAudioTrack = settings.rememberAudioTrack,
            rememberSubtitleTrack = settings.rememberSubtitleTrack,
            showNextVideoInfo = settings.showNextVideoInfo,
            audioNormalization = settings.audioNormalization,
            subtitleLanguage = settings.subtitleLanguage,
            subtitleMode = settings.subtitleMode,
            subtitleTextSize = settings.subtitleTextSize,
            subtitleTextWeight = settings.subtitleTextWeight,
            subtitleFontStyle = settings.subtitleFontStyle,
            subtitleTextColor = settings.subtitleTextColor,
            subtitleBackgroundColor = settings.subtitleBackgroundColor,
            subtitleShadow = settings.subtitleShadow,
            subtitleVerticalPosition = settings.subtitleVerticalPosition,
            appLanguage = settings.appLanguage,
            appTheme = settings.appTheme,
            posterDensity = settings.posterDensity,
            imageQuality = settings.imageQuality,
            metadataDisplay = settings.metadataDisplay,
            homeSectionOrder = settings.homeSectionOrder,
            homeEnabledSections = settings.homeEnabledSections,
        )
}
