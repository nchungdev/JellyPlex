package org.jellyplus.client.domain.usecases

import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

class SetAutoSkipOutroUseCase(private val settings: PlayerSettingsLocalDataSource) {
    operator fun invoke(enabled: Boolean) { settings.autoSkipOutro = enabled }
}
