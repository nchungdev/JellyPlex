package org.jellyplus.client.data.datasource.local

import com.russhwolf.settings.Settings

class PlayerSettingsLocalDataSource(private val settings: Settings = Settings()) {
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
        private const val KEY_SEEK_BACK_SECONDS = "player_seek_back_seconds"
        private const val KEY_SEEK_FORWARD_SECONDS = "player_seek_forward_seconds"
    }

    var autoSkipIntro: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_INTRO, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_INTRO, value)

    var autoSkipOutro: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_OUTRO, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_OUTRO, value)

    var autoSkipRecap: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_RECAP, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_RECAP, value)

    var autoSkipPreview: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_PREVIEW, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_PREVIEW, value)

    var autoNext: Boolean
        get() = settings.getBoolean(KEY_AUTO_NEXT, true)
        set(value) = settings.putBoolean(KEY_AUTO_NEXT, value)

    var playbackSpeed: Float
        get() = settings.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
        set(value) = settings.putFloat(KEY_PLAYBACK_SPEED, value)

    var autoPictureInPicture: Boolean
        get() = settings.getBoolean(KEY_AUTO_PICTURE_IN_PICTURE, true)
        set(value) = settings.putBoolean(KEY_AUTO_PICTURE_IN_PICTURE, value)

    var seamlessTransition: Boolean
        get() = settings.getBoolean(KEY_SEAMLESS_TRANSITION, false)
        set(value) = settings.putBoolean(KEY_SEAMLESS_TRANSITION, value)

    var preferOriginalAudio: Boolean
        get() = settings.getBoolean(KEY_PREFER_ORIGINAL_AUDIO, true)
        set(value) = settings.putBoolean(KEY_PREFER_ORIGINAL_AUDIO, value)

    var showGestureHints: Boolean
        get() = settings.getBoolean(KEY_SHOW_GESTURE_HINTS, true)
        set(value) = settings.putBoolean(KEY_SHOW_GESTURE_HINTS, value)

    var seekBackSeconds: Int
        get() = settings.getInt(KEY_SEEK_BACK_SECONDS, 5)
        set(value) = settings.putInt(KEY_SEEK_BACK_SECONDS, value)

    var seekForwardSeconds: Int
        get() = settings.getInt(KEY_SEEK_FORWARD_SECONDS, 10)
        set(value) = settings.putInt(KEY_SEEK_FORWARD_SECONDS, value)
}
