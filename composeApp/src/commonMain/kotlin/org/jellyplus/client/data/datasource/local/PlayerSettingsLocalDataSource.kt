package org.jellyplus.client.data.datasource.local

import com.russhwolf.settings.Settings

class PlayerSettingsLocalDataSource(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_AUTO_SKIP_INTRO = "player_auto_skip_intro"
        private const val KEY_AUTO_SKIP_OUTRO = "player_auto_skip_outro"
        private const val KEY_AUTO_SKIP_PREVIEW = "player_auto_skip_preview"
        private const val KEY_AUTO_NEXT = "player_auto_next"
        private const val KEY_PLAYBACK_SPEED = "player_playback_speed"
    }

    var autoSkipIntro: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_INTRO, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_INTRO, value)

    var autoSkipOutro: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_OUTRO, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_OUTRO, value)

    var autoSkipPreview: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_PREVIEW, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_PREVIEW, value)

    var autoNext: Boolean
        get() = settings.getBoolean(KEY_AUTO_NEXT, false)
        set(value) = settings.putBoolean(KEY_AUTO_NEXT, value)

    var playbackSpeed: Float
        get() = settings.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
        set(value) = settings.putFloat(KEY_PLAYBACK_SPEED, value)
}
