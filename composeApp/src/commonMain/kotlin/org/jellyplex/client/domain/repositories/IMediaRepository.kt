package org.jellyplex.client.domain.repositories

import org.jellyplex.client.domain.models.HomeContent
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.models.Person
import org.jellyplex.client.domain.models.PlaybackConfig

interface IMediaRepository {
    suspend fun getMovies(): List<MediaItem>
    suspend fun getTvShows(): List<MediaItem>
    suspend fun getResumeItems(userId: String): List<MediaItem>
    suspend fun getRecentlyAddedItems(userId: String): List<MediaItem>
    suspend fun searchItems(query: String): List<MediaItem>
    suspend fun resolveStreamConfig(item: MediaItem, userId: String, deviceId: String): PlaybackConfig?
    suspend fun getItemDetails(itemId: String, userId: String): MediaItem
    suspend fun getPeople(itemId: String): List<Person>
    suspend fun getSeasons(seriesId: String): List<MediaItem>
    suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem>
    fun getBaseUrl(): String
    fun getAccessToken(): String?

    // Cache methods
    suspend fun getHomeCache(): HomeContent?
    suspend fun saveHomeCache(content: HomeContent)
    suspend fun getMoviesCache(): List<MediaItem>?
    suspend fun saveMoviesCache(items: List<MediaItem>)
    suspend fun getTvShowsCache(): List<MediaItem>?
    suspend fun saveTvShowsCache(items: List<MediaItem>)

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
