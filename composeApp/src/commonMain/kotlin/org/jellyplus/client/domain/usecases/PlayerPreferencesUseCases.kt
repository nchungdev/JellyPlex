package org.jellyplus.client.domain.usecases

import org.jellyplus.client.domain.repositories.IPlayerSettingsRepository

/**
 * Facade use-case that provides a single injection point for all player/app preference
 * operations. ViewModels depend on this class rather than [IPlayerSettingsRepository] directly,
 * keeping the strict View → ViewModel → UseCase → Repository layering intact.
 */
class PlayerPreferencesUseCases(private val repository: IPlayerSettingsRepository) {

    // ── Reads ─────────────────────────────────────────────────────────────────
    val autoSkipIntro get() = repository.autoSkipIntro
    val autoSkipOutro get() = repository.autoSkipOutro
    val autoSkipRecap get() = repository.autoSkipRecap
    val autoSkipPreview get() = repository.autoSkipPreview
    val autoNext get() = repository.autoNext
    val autoPictureInPicture get() = repository.autoPictureInPicture
    val seamlessTransition get() = repository.seamlessTransition
    val preferOriginalAudio get() = repository.preferOriginalAudio
    val showGestureHints get() = repository.showGestureHints
    val doubleTapSeekEnabled get() = repository.doubleTapSeekEnabled
    val playbackSpeed get() = repository.playbackSpeed
    val seekBackSeconds get() = repository.seekBackSeconds
    val seekForwardSeconds get() = repository.seekForwardSeconds
    val holdSpeedEnabled get() = repository.holdSpeedEnabled
    val holdSpeedMultiplier get() = repository.holdSpeedMultiplier
    val swipeLeftBrightnessEnabled get() = repository.swipeLeftBrightnessEnabled
    val swipeRightVolumeEnabled get() = repository.swipeRightVolumeEnabled
    val maxAudioChannels get() = repository.maxAudioChannels
    val preferredAudioLanguage get() = repository.preferredAudioLanguage
    val playDefaultAudioTrack get() = repository.playDefaultAudioTrack
    val internetVideoQuality get() = repository.internetVideoQuality
    val maxTranscodeResolution get() = repository.maxTranscodeResolution
    val limitMaxVideoResolution get() = repository.limitMaxVideoResolution
    val musicInternetQuality get() = repository.musicInternetQuality
    val rememberAudioTrack get() = repository.rememberAudioTrack
    val rememberSubtitleTrack get() = repository.rememberSubtitleTrack
    val showNextVideoInfo get() = repository.showNextVideoInfo
    val audioNormalization get() = repository.audioNormalization
    val subtitleLanguage get() = repository.subtitleLanguage
    val subtitleMode get() = repository.subtitleMode
    val subtitleTextSize get() = repository.subtitleTextSize
    val subtitleTextWeight get() = repository.subtitleTextWeight
    val subtitleFontStyle get() = repository.subtitleFontStyle
    val subtitleTextColor get() = repository.subtitleTextColor
    val subtitleBackgroundColor get() = repository.subtitleBackgroundColor
    val subtitleShadow get() = repository.subtitleShadow
    val subtitleVerticalPosition get() = repository.subtitleVerticalPosition
    val appLanguage get() = repository.appLanguage
    val appTheme get() = repository.appTheme
    val posterDensity get() = repository.posterDensity
    val imageQuality get() = repository.imageQuality
    val metadataDisplay get() = repository.metadataDisplay
    val homeSectionOrder get() = repository.homeSectionOrder
    val homeEnabledSections get() = repository.homeEnabledSections

    // ── Writes ────────────────────────────────────────────────────────────────
    fun setAutoSkipIntro(v: Boolean) { repository.autoSkipIntro = v }
    fun setAutoSkipOutro(v: Boolean) { repository.autoSkipOutro = v }
    fun setAutoSkipRecap(v: Boolean) { repository.autoSkipRecap = v }
    fun setAutoSkipPreview(v: Boolean) { repository.autoSkipPreview = v }
    fun setAutoNext(v: Boolean) { repository.autoNext = v }
    fun setAutoPictureInPicture(v: Boolean) { repository.autoPictureInPicture = v }
    fun setSeamlessTransition(v: Boolean) { repository.seamlessTransition = v }
    fun setPreferOriginalAudio(v: Boolean) { repository.preferOriginalAudio = v }
    fun setShowGestureHints(v: Boolean) { repository.showGestureHints = v }
    fun setDoubleTapSeekEnabled(v: Boolean) { repository.doubleTapSeekEnabled = v }
    fun setPlaybackSpeed(v: Float) { repository.playbackSpeed = v }
    fun setSeekBackSeconds(v: Int) { repository.seekBackSeconds = v }
    fun setSeekForwardSeconds(v: Int) { repository.seekForwardSeconds = v }
    fun setHoldSpeedEnabled(v: Boolean) { repository.holdSpeedEnabled = v }
    fun setHoldSpeedMultiplier(v: String) { repository.holdSpeedMultiplier = v }
    fun setSwipeLeftBrightnessEnabled(v: Boolean) { repository.swipeLeftBrightnessEnabled = v }
    fun setSwipeRightVolumeEnabled(v: Boolean) { repository.swipeRightVolumeEnabled = v }
    fun setMaxAudioChannels(v: String) { repository.maxAudioChannels = v }
    fun setPreferredAudioLanguage(v: String) { repository.preferredAudioLanguage = v }
    fun setPlayDefaultAudioTrack(v: Boolean) { repository.playDefaultAudioTrack = v }
    fun setInternetVideoQuality(v: String) { repository.internetVideoQuality = v }
    fun setMaxTranscodeResolution(v: String) { repository.maxTranscodeResolution = v }
    fun setLimitMaxVideoResolution(v: Boolean) { repository.limitMaxVideoResolution = v }
    fun setMusicInternetQuality(v: String) { repository.musicInternetQuality = v }
    fun setRememberAudioTrack(v: Boolean) { repository.rememberAudioTrack = v }
    fun setRememberSubtitleTrack(v: Boolean) { repository.rememberSubtitleTrack = v }
    fun setShowNextVideoInfo(v: Boolean) { repository.showNextVideoInfo = v }
    fun setAudioNormalization(v: String) { repository.audioNormalization = v }
    fun setSubtitleLanguage(v: String) { repository.subtitleLanguage = v }
    fun setSubtitleMode(v: String) { repository.subtitleMode = v }
    fun setSubtitleTextSize(v: String) { repository.subtitleTextSize = v }
    fun setSubtitleTextWeight(v: String) { repository.subtitleTextWeight = v }
    fun setSubtitleFontStyle(v: String) { repository.subtitleFontStyle = v }
    fun setSubtitleTextColor(v: String) { repository.subtitleTextColor = v }
    fun setSubtitleBackgroundColor(v: String) { repository.subtitleBackgroundColor = v }
    fun setSubtitleShadow(v: String) { repository.subtitleShadow = v }
    fun setSubtitleVerticalPosition(v: String) { repository.subtitleVerticalPosition = v }
    fun setAppLanguage(v: String) { repository.appLanguage = v }
    fun setAppTheme(v: String) { repository.appTheme = v }
    fun setPosterDensity(v: String) { repository.posterDensity = v }
    fun setImageQuality(v: String) { repository.imageQuality = v }
    fun setMetadataDisplay(v: String) { repository.metadataDisplay = v }
    fun setHomeSectionOrder(v: String) { repository.homeSectionOrder = v }
    fun setHomeEnabledSections(v: String) { repository.homeEnabledSections = v }
}
