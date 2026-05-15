package org.jellyplus.client.domain.repositories

/** Domain-layer abstraction over persistent player preferences. */
interface IPlayerSettingsRepository {
    // Playback behaviour
    var autoSkipIntro: Boolean
    var autoSkipOutro: Boolean
    var autoSkipRecap: Boolean
    var autoSkipPreview: Boolean
    var autoNext: Boolean
    var autoPictureInPicture: Boolean
    var seamlessTransition: Boolean
    var preferOriginalAudio: Boolean
    var showNextVideoInfo: Boolean
    var playbackSpeed: Float
    var seekBackSeconds: Int
    var seekForwardSeconds: Int

    // Audio / streaming quality
    var maxAudioChannels: String
    var preferredAudioLanguage: String
    var playDefaultAudioTrack: Boolean
    var rememberAudioTrack: Boolean
    var internetVideoQuality: String
    var maxTranscodeResolution: String
    var limitMaxVideoResolution: Boolean
    var musicInternetQuality: String
    var audioNormalization: String

    // Subtitles
    var subtitleLanguage: String
    var subtitleMode: String
    var rememberSubtitleTrack: Boolean
    var subtitleTextSize: String
    var subtitleTextWeight: String
    var subtitleFontStyle: String
    var subtitleTextColor: String
    var subtitleBackgroundColor: String
    var subtitleShadow: String
    var subtitleVerticalPosition: String

    // Display / UI
    var appLanguage: String
    var appTheme: String
    var posterDensity: String
    var imageQuality: String
    var metadataDisplay: String

    // Controls / gestures
    var showGestureHints: Boolean
    var doubleTapSeekEnabled: Boolean
    var holdSpeedEnabled: Boolean
    var holdSpeedMultiplier: String
    var swipeLeftBrightnessEnabled: Boolean
    var swipeRightVolumeEnabled: Boolean

    // Home sections
    var homeSectionOrder: String
    var homeEnabledSections: String
}
