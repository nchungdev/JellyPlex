package org.jellyplus.client.domain.usecases

import org.jellyplus.client.data.datasource.local.PlayerSettingsLocalDataSource

class GetAutoSkipPreviewUseCase(private val settings: PlayerSettingsLocalDataSource) {
    operator fun invoke(): Boolean = settings.autoSkipPreview
}
