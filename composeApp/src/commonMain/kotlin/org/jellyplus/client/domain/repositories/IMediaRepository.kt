package org.jellyplus.client.domain.repositories

import org.jellyplus.client.domain.models.HomeContent
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.Person
import org.jellyplus.client.domain.models.PlaybackConfig
import kotlinx.coroutines.flow.StateFlow
import org.jellyplus.client.domain.models.*

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
    suspend fun getWatchHistory(userId: String): List<MediaItem>
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
    suspend fun markItemAsPlayed(userId: String, itemId: String)
    suspend fun saveCustomMarker(itemId: String, startTicks: Long, endTicks: Long)
    suspend fun getIntroMarkers(itemId: String): List<IntroMarker>
}
