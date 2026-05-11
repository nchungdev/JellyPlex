package org.jellyplex.client.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class HomeContent(
    val resumeItems: List<MediaItem>,
    val recentlyAddedItems: List<MediaItem>
)
