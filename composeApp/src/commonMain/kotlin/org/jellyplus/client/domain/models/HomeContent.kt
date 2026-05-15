package org.jellyplus.client.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class HomeContent(
    val featuredItems: List<MediaItem> = emptyList(),
    val resumeItems: List<MediaItem>,
    val recentlyAddedItems: List<MediaItem>
)

data class PagedMediaItems(
    val items: List<MediaItem>,
    val totalRecordCount: Int,
)
