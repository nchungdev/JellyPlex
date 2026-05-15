package org.jellyplus.client.data.datasource.local

import com.russhwolf.settings.Settings
import org.jellyplus.client.domain.repositories.IPlayerSettingsRepository

class PlayerSettingsLocalDataSource(private val settings: Settings = Settings()) : IPlayerSettingsRepository {
    companion object {
        private const val KEY_AUTO_SKIP_INTRO = "player_auto_skip_intro"
        private const val KEY_AUTO_SKIP_OUTRO = "player_auto_skip_outro"
        private const val KEY_AUTO_SKIP_RECAP = "player_auto_skip_recap"
        private const val KEY_AUTO_SKIP_PREVIEW = "player_auto_skip_preview"
        private const val KEY_AUTO_NEXT = "player_auto_next"
        private const val KEY_PLAYBACK_SPEED = "player_playback_speed"
        private const val KEY_AUTO_PICTURE_IN_PICTURE = "player_auto_picture_in_picture"
        private const val KEY_SEAMLESS_TRANSITION = "player_seamless_transition"
        private const val KEY_PREFER_ORIGINAL_AUDIO = "player_prefer_original_audio"
        private const val KEY_SHOW_GESTURE_HINTS = "player_show_gesture_hints"
        private const val KEY_DOUBLE_TAP_SEEK_ENABLED = "controls_double_tap_seek_enabled"
        private const val KEY_SEEK_BACK_SECONDS = "player_seek_back_seconds"
        private const val KEY_SEEK_FORWARD_SECONDS = "player_seek_forward_seconds"
        private const val KEY_HOLD_SPEED_ENABLED = "controls_hold_speed_enabled"
        private const val KEY_HOLD_SPEED_MULTIPLIER = "controls_hold_speed_multiplier"
        private const val KEY_SWIPE_LEFT_BRIGHTNESS_ENABLED = "controls_swipe_left_brightness_enabled"
        private const val KEY_SWIPE_RIGHT_VOLUME_ENABLED = "controls_swipe_right_volume_enabled"
        private const val KEY_MAX_AUDIO_CHANNELS = "player_max_audio_channels"
        private const val KEY_PREFERRED_AUDIO_LANGUAGE = "player_preferred_audio_language"
        private const val KEY_PLAY_DEFAULT_AUDIO_TRACK = "player_play_default_audio_track"
        private const val KEY_INTERNET_VIDEO_QUALITY = "player_internet_video_quality"
        private const val KEY_MAX_TRANSCODE_RESOLUTION = "player_max_transcode_resolution"
        private const val KEY_LIMIT_MAX_VIDEO_RESOLUTION = "player_limit_max_video_resolution"
        private const val KEY_MUSIC_INTERNET_QUALITY = "player_music_internet_quality"
        private const val KEY_REMEMBER_AUDIO_TRACK = "player_remember_audio_track"
        private const val KEY_REMEMBER_SUBTITLE_TRACK = "player_remember_subtitle_track"
        private const val KEY_SHOW_NEXT_VIDEO_INFO = "player_show_next_video_info"
        private const val KEY_AUDIO_NORMALIZATION = "player_audio_normalization"
        private const val KEY_SUBTITLE_LANGUAGE = "player_subtitle_language"
        private const val KEY_SUBTITLE_MODE = "player_subtitle_mode"
        private const val KEY_SUBTITLE_TEXT_SIZE = "player_subtitle_text_size"
        private const val KEY_SUBTITLE_TEXT_WEIGHT = "player_subtitle_text_weight"
        private const val KEY_SUBTITLE_FONT_STYLE = "player_subtitle_font_style"
        private const val KEY_SUBTITLE_TEXT_COLOR = "player_subtitle_text_color"
        private const val KEY_SUBTITLE_BACKGROUND_COLOR = "player_subtitle_background_color"
        private const val KEY_SUBTITLE_SHADOW = "player_subtitle_shadow"
        private const val KEY_SUBTITLE_VERTICAL_POSITION = "player_subtitle_vertical_position"
        private const val KEY_APP_LANGUAGE = "display_app_language"
        private const val KEY_APP_THEME = "display_app_theme"
        private const val KEY_POSTER_DENSITY = "display_poster_density"
        private const val KEY_IMAGE_QUALITY = "display_image_quality"
        private const val KEY_METADATA_DISPLAY = "display_metadata_display"
        private const val KEY_HOME_SECTION_ORDER = "home_section_order"
        private const val KEY_HOME_ENABLED_SECTIONS = "home_enabled_sections"
        private const val DEFAULT_HOME_SECTIONS = "hero,continue,recent,genreRows,movies,tv,genres"
    }

    override var autoSkipIntro: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_INTRO, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_INTRO, value)

    override var autoSkipOutro: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_OUTRO, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_OUTRO, value)

    override var autoSkipRecap: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_RECAP, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_RECAP, value)

    override var autoSkipPreview: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_PREVIEW, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_PREVIEW, value)

    override var autoNext: Boolean
        get() = settings.getBoolean(KEY_AUTO_NEXT, true)
        set(value) = settings.putBoolean(KEY_AUTO_NEXT, value)

    override var playbackSpeed: Float
        get() = settings.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
        set(value) = settings.putFloat(KEY_PLAYBACK_SPEED, value)

    override var autoPictureInPicture: Boolean
        get() = settings.getBoolean(KEY_AUTO_PICTURE_IN_PICTURE, true)
        set(value) = settings.putBoolean(KEY_AUTO_PICTURE_IN_PICTURE, value)

    override var seamlessTransition: Boolean
        get() = settings.getBoolean(KEY_SEAMLESS_TRANSITION, false)
        set(value) = settings.putBoolean(KEY_SEAMLESS_TRANSITION, value)

    override var preferOriginalAudio: Boolean
        get() = settings.getBoolean(KEY_PREFER_ORIGINAL_AUDIO, true)
        set(value) = settings.putBoolean(KEY_PREFER_ORIGINAL_AUDIO, value)

    override var showGestureHints: Boolean
        get() = settings.getBoolean(KEY_SHOW_GESTURE_HINTS, true)
        set(value) = settings.putBoolean(KEY_SHOW_GESTURE_HINTS, value)

    override var doubleTapSeekEnabled: Boolean
        get() = settings.getBoolean(KEY_DOUBLE_TAP_SEEK_ENABLED, true)
        set(value) = settings.putBoolean(KEY_DOUBLE_TAP_SEEK_ENABLED, value)

    override var seekBackSeconds: Int
        get() = settings.getInt(KEY_SEEK_BACK_SECONDS, 5)
        set(value) = settings.putInt(KEY_SEEK_BACK_SECONDS, value)

    override var seekForwardSeconds: Int
        get() = settings.getInt(KEY_SEEK_FORWARD_SECONDS, 10)
        set(value) = settings.putInt(KEY_SEEK_FORWARD_SECONDS, value)

    override var holdSpeedEnabled: Boolean
        get() = settings.getBoolean(KEY_HOLD_SPEED_ENABLED, true)
        set(value) = settings.putBoolean(KEY_HOLD_SPEED_ENABLED, value)

    override var holdSpeedMultiplier: String
        get() = settings.getString(KEY_HOLD_SPEED_MULTIPLIER, "2x")
        set(value) = settings.putString(KEY_HOLD_SPEED_MULTIPLIER, value)

    override var swipeLeftBrightnessEnabled: Boolean
        get() = settings.getBoolean(KEY_SWIPE_LEFT_BRIGHTNESS_ENABLED, true)
        set(value) = settings.putBoolean(KEY_SWIPE_LEFT_BRIGHTNESS_ENABLED, value)

    override var swipeRightVolumeEnabled: Boolean
        get() = settings.getBoolean(KEY_SWIPE_RIGHT_VOLUME_ENABLED, true)
        set(value) = settings.putBoolean(KEY_SWIPE_RIGHT_VOLUME_ENABLED, value)

    override var maxAudioChannels: String
        get() = settings.getString(KEY_MAX_AUDIO_CHANNELS, "Auto")
        set(value) = settings.putString(KEY_MAX_AUDIO_CHANNELS, value)

    override var preferredAudioLanguage: String
        get() = settings.getString(KEY_PREFERRED_AUDIO_LANGUAGE, "Any")
        set(value) = settings.putString(KEY_PREFERRED_AUDIO_LANGUAGE, value)

    override var playDefaultAudioTrack: Boolean
        get() = settings.getBoolean(KEY_PLAY_DEFAULT_AUDIO_TRACK, false)
        set(value) = settings.putBoolean(KEY_PLAY_DEFAULT_AUDIO_TRACK, value)

    override var internetVideoQuality: String
        get() = settings.getString(KEY_INTERNET_VIDEO_QUALITY, "Auto")
        set(value) = settings.putString(KEY_INTERNET_VIDEO_QUALITY, value)

    override var maxTranscodeResolution: String
        get() = settings.getString(KEY_MAX_TRANSCODE_RESOLUTION, "Auto")
        set(value) = settings.putString(KEY_MAX_TRANSCODE_RESOLUTION, value)

    override var limitMaxVideoResolution: Boolean
        get() = settings.getBoolean(KEY_LIMIT_MAX_VIDEO_RESOLUTION, false)
        set(value) = settings.putBoolean(KEY_LIMIT_MAX_VIDEO_RESOLUTION, value)

    override var musicInternetQuality: String
        get() = settings.getString(KEY_MUSIC_INTERNET_QUALITY, "Auto")
        set(value) = settings.putString(KEY_MUSIC_INTERNET_QUALITY, value)

    override var rememberAudioTrack: Boolean
        get() = settings.getBoolean(KEY_REMEMBER_AUDIO_TRACK, true)
        set(value) = settings.putBoolean(KEY_REMEMBER_AUDIO_TRACK, value)

    override var rememberSubtitleTrack: Boolean
        get() = settings.getBoolean(KEY_REMEMBER_SUBTITLE_TRACK, true)
        set(value) = settings.putBoolean(KEY_REMEMBER_SUBTITLE_TRACK, value)

    override var showNextVideoInfo: Boolean
        get() = settings.getBoolean(KEY_SHOW_NEXT_VIDEO_INFO, true)
        set(value) = settings.putBoolean(KEY_SHOW_NEXT_VIDEO_INFO, value)

    override var audioNormalization: String
        get() = settings.getString(KEY_AUDIO_NORMALIZATION, "Off")
        set(value) = settings.putString(KEY_AUDIO_NORMALIZATION, value)

    override var subtitleLanguage: String
        get() = settings.getString(KEY_SUBTITLE_LANGUAGE, "Any")
        set(value) = settings.putString(KEY_SUBTITLE_LANGUAGE, value)

    override var subtitleMode: String
        get() = settings.getString(KEY_SUBTITLE_MODE, "Default")
        set(value) = settings.putString(KEY_SUBTITLE_MODE, value)

    override var subtitleTextSize: String
        get() = settings.getString(KEY_SUBTITLE_TEXT_SIZE, "Normal")
        set(value) = settings.putString(KEY_SUBTITLE_TEXT_SIZE, value)

    override var subtitleTextWeight: String
        get() = settings.getString(KEY_SUBTITLE_TEXT_WEIGHT, "Normal")
        set(value) = settings.putString(KEY_SUBTITLE_TEXT_WEIGHT, value)

    override var subtitleFontStyle: String
        get() = settings.getString(KEY_SUBTITLE_FONT_STYLE, "Default")
        set(value) = settings.putString(KEY_SUBTITLE_FONT_STYLE, value)

    override var subtitleTextColor: String
        get() = settings.getString(KEY_SUBTITLE_TEXT_COLOR, "White")
        set(value) = settings.putString(KEY_SUBTITLE_TEXT_COLOR, value)

    override var subtitleBackgroundColor: String
        get() = settings.getString(KEY_SUBTITLE_BACKGROUND_COLOR, "Black")
        set(value) = settings.putString(KEY_SUBTITLE_BACKGROUND_COLOR, value)

    override var subtitleShadow: String
        get() = settings.getString(KEY_SUBTITLE_SHADOW, "Drop shadow")
        set(value) = settings.putString(KEY_SUBTITLE_SHADOW, value)

    override var subtitleVerticalPosition: String
        get() = settings.getString(KEY_SUBTITLE_VERTICAL_POSITION, "Bottom")
        set(value) = settings.putString(KEY_SUBTITLE_VERTICAL_POSITION, value)

    override var appLanguage: String
        get() = settings.getString(KEY_APP_LANGUAGE, "System")
        set(value) = settings.putString(KEY_APP_LANGUAGE, value)

    override var appTheme: String
        get() = settings.getString(KEY_APP_THEME, "Dark")
        set(value) = settings.putString(KEY_APP_THEME, value)

    override var posterDensity: String
        get() = settings.getString(KEY_POSTER_DENSITY, "Comfortable")
        set(value) = settings.putString(KEY_POSTER_DENSITY, value)

    override var imageQuality: String
        get() = settings.getString(KEY_IMAGE_QUALITY, "Auto")
        set(value) = settings.putString(KEY_IMAGE_QUALITY, value)

    override var metadataDisplay: String
        get() = settings.getString(KEY_METADATA_DISPLAY, "Title and progress")
        set(value) = settings.putString(KEY_METADATA_DISPLAY, value)

    override var homeSectionOrder: String
        get() = settings.getString(KEY_HOME_SECTION_ORDER, DEFAULT_HOME_SECTIONS)
        set(value) = settings.putString(KEY_HOME_SECTION_ORDER, value)

    override var homeEnabledSections: String
        get() = settings.getString(KEY_HOME_ENABLED_SECTIONS, DEFAULT_HOME_SECTIONS)
        set(value) = settings.putString(KEY_HOME_ENABLED_SECTIONS, value)
}
