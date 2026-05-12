package org.jellyplus.client.domain.usecases

import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

class SetAutoNextUseCase(private val settings: PlayerSettingsLocalDataSource) {
    operator fun invoke(enabled: Boolean) { settings.autoNext = enabled }
}
