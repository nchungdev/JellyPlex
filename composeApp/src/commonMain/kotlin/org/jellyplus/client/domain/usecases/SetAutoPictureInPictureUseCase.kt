package org.jellyplus.client.domain.usecases

import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

class SetAutoPictureInPictureUseCase(private val settings: PlayerSettingsLocalDataSource) {
    operator fun invoke(enabled: Boolean) {
        settings.autoPictureInPicture = enabled
    }
}
