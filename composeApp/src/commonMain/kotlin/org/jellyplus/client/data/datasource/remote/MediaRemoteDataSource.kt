package org.jellyplus.client.data.datasource.remote

import org.jellyplus.client.data.remote.JellyfinApi
import org.jellyplus.client.data.remote.models.PlaybackInfoResponse
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.Person

interface IMediaRemoteDataSource {
    suspend fun getMovies(): List<MediaItem>
    suspend fun getTvShows(): List<MediaItem>
    suspend fun getHomeRecentlyAdded(userId: String): List<MediaItem>
    suspend fun getHomeResume(userId: String): List<MediaItem>
    suspend fun searchItems(query: String): List<MediaItem>
    suspend fun getItemDetails(itemId: String, userId: String): MediaItem
    suspend fun getPeople(itemId: String): List<Person>
    suspend fun getSeasons(seriesId: String): List<MediaItem>
    suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem>
    suspend fun getPlaybackInfo(itemId: String, userId: String): PlaybackInfoResponse
    fun buildUrl(block: io.ktor.http.URLBuilder.() -> Unit): String
    fun getVideoStreamUrl(itemId: String): String
    suspend fun reportPlaybackStart(itemId: String, playSessionId: String)
    suspend fun reportPlaybackProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean
    )

    suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long)
    fun getBaseUrl(): String
    fun getAccessToken(): String?
}

class MediaRemoteDataSource(private val api: JellyfinApi) : IMediaRemoteDataSource {
    override suspend fun getMovies(): List<MediaItem> = api.getMovies()
    override suspend fun getTvShows(): List<MediaItem> = api.getTvShows()
    override suspend fun getHomeRecentlyAdded(userId: String): List<MediaItem> = api.getRecentlyAddedItems(userId)
    override suspend fun getHomeResume(userId: String): List<MediaItem> = api.getResumeItems(userId)
    override suspend fun searchItems(query: String): List<MediaItem> = api.searchItems(query)
    override suspend fun getItemDetails(itemId: String, userId: String): MediaItem = api.getItemDetails(itemId, userId)
    override suspend fun getPeople(itemId: String): List<Person> = api.getPeople(itemId)
    override suspend fun getSeasons(seriesId: String): List<MediaItem> = api.getSeasons(seriesId)
    override suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem> =
        api.getEpisodes(seriesId, seasonId)

    override suspend fun getPlaybackInfo(itemId: String, userId: String): PlaybackInfoResponse =
        api.getPlaybackInfo(itemId, userId)

    override fun buildUrl(block: io.ktor.http.URLBuilder.() -> Unit): String = api.buildUrl(block = block)
    override fun getVideoStreamUrl(itemId: String): String = api.getVideoStreamUrl(itemId)
    override suspend fun reportPlaybackStart(itemId: String, playSessionId: String) =
        api.reportPlaybackStart(itemId, playSessionId)

    override suspend fun reportPlaybackProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean
    ) = api.reportPlaybackProgress(itemId, playSessionId, positionTicks, isPaused, isMuted)

    override suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long) =
        api.reportPlaybackStopped(itemId, playSessionId, positionTicks)

    override fun getBaseUrl(): String = api.baseUrl
    override fun getAccessToken(): String? = api.accessToken
}
