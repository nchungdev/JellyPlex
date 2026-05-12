package org.jellyplus.client.domain.usecases

import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

class GetPlaybackSpeedUseCase(private val settings: PlayerSettingsLocalDataSource) {
    operator fun invoke(): Float = settings.playbackSpeed
}
