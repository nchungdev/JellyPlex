package org.jellyplus.client.domain.usecases

import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

class GetAutoNextUseCase(private val settings: PlayerSettingsLocalDataSource) {
    operator fun invoke(): Boolean = settings.autoNext
}
