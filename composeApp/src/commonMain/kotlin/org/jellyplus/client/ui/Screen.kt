package org.jellyplus.client.ui

import kotlinx.serialization.Serializable
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType

@Serializable
sealed class Screen {
    @Serializable
    object Home : Screen()

    @Serializable
    data class Details(val item: MediaItem, val focusSeasonId: String? = null) : Screen()

    @Serializable
    data class Listing(
        val type: MediaType,
        val title: String,
        /** Non-null when opened from a genre chip — listing is pre-filtered to this genre. */
        val genre: String? = null,
    ) : Screen()

    @Serializable
    data class Player(
        val item: MediaItem,
        val playlist: List<MediaItem> = emptyList(),
        val parentItem: MediaItem? = null
    ) : Screen()

    @Serializable
    object Search : Screen()

    @Serializable
    object Settings : Screen()
}
