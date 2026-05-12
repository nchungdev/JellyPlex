package org.jellyplus.client.data.datasource.local

import com.russhwolf.settings.Settings

class PlayerSettingsLocalDataSource(private val settings: Settings = Settings()) {
    companion object {
        private const val KEY_AUTO_SKIP_INTRO = "player_auto_skip_intro"
    }

    var autoSkipIntro: Boolean
        get() = settings.getBoolean(KEY_AUTO_SKIP_INTRO, false)
        set(value) = settings.putBoolean(KEY_AUTO_SKIP_INTRO, value)
}
