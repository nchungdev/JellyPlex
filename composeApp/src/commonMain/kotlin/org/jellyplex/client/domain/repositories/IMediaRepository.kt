package org.jellyplex.client.domain.repositories

import org.jellyplex.client.domain.models.HomeContent
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.models.Person
import org.jellyplex.client.domain.models.PlaybackConfig
import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.domain.models.*

interface IMediaRepository {
    // SSOT Flows
    val movies: StateFlow<List<MediaItem>?>
    val tvShows: StateFlow<List<MediaItem>?>
    val homeContent: StateFlow<HomeContent?>

    // Refresh actions
    suspend fun refreshMovies(): Result<Unit>
    suspend fun refreshTvShows(): Result<Unit>
    suspend fun refreshHomeContent(userId: String): Result<Unit>

    // Other actions
    suspend fun searchItems(query: String): List<MediaItem>
    suspend fun resolveStreamConfig(item: MediaItem, userId: String, deviceId: String): PlaybackConfig?
    suspend fun getItemDetails(itemId: String, userId: String): MediaItem
    suspend fun getPeople(itemId: String): List<Person>
    suspend fun getSeasons(seriesId: String): List<MediaItem>
    suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem>

    fun getBaseUrl(): String
    fun getAccessToken(): String?
    fun clearCache()

    // Playback Reporting
    suspend fun reportPlaybackStart(itemId: String, playSessionId: String)
    suspend fun reportPlaybackProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean = false
    )

    suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long)
}
