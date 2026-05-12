package org.jellyplus.client.domain.usecases

import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

class SetPlaybackSpeedUseCase(private val settings: PlayerSettingsLocalDataSource) {
    operator fun invoke(speed: Float) { settings.playbackSpeed = speed }
}
