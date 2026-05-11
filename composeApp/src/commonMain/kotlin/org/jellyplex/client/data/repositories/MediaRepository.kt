package org.jellyplex.client.data.repositories

import io.ktor.http.takeFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jellyplex.client.data.local.SessionManager
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.domain.models.*
import org.jellyplex.client.domain.repositories.IMediaRepository

class MediaRepository(
    private val api: JellyfinApi,
    private val sessionManager: SessionManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : IMediaRepository {
    override suspend fun getMovies(): List<MediaItem> = api.getMovies()

    override suspend fun getTvShows(): List<MediaItem> = api.getTvShows()

    override suspend fun getResumeItems(userId: String): List<MediaItem> = api.getResumeItems(userId)

    override suspend fun getRecentlyAddedItems(userId: String): List<MediaItem> = api.getRecentlyAddedItems(userId)

    override suspend fun searchItems(query: String): List<MediaItem> = api.searchItems(query)

    override suspend fun resolveStreamConfig(item: MediaItem, userId: String, deviceId: String): PlaybackConfig? {
        val itemId = item.id

        return try {
            val info = api.getPlaybackInfo(itemId, userId)
            val source = info.mediaSources.firstOrNull()
            val playSessionId = info.playSessionId

            if (source != null) {
                val isAudio = item.type == MediaType.AUDIO
                val typePath = if (isAudio) "audio" else "videos"

                val url = when {
                    // Case 0: Remote source (.strm file)
                    source.isRemote && source.path?.startsWith("http") == true -> {
                        source.path
                    }
                    // Case 1: Transcoding
                    !source.transcodingUrl.isNullOrEmpty() -> {
                        if (source.transcodingUrl.startsWith("http")) {
                            source.transcodingUrl
                        } else {
                            api.buildUrl { takeFrom(source.transcodingUrl) }
                        }
                    }
                    // Case 2: Direct
                    source.supportsDirectPlay || source.supportsDirectStream -> {
                        val containerLower = source.container?.lowercase()
                        if (containerLower == "hls" || containerLower == "m3u8") {
                            api.buildUrl(typePath, itemId, "master.m3u8") {
                                parameters.append("MediaSourceId", source.id)
                                parameters.append("DeviceId", deviceId)
                            }
                        } else {
                            val ext = source.container?.let { ".$it" } ?: ""
                            api.buildUrl(typePath, itemId, "stream$ext") {
                                parameters.append("Static", "true")
                                parameters.append("MediaSourceId", source.id)
                                parameters.append("DeviceId", deviceId)
                            }
                        }
                    }

                    else -> api.getVideoStreamUrl(itemId)
                }
                PlaybackConfig(
                    url = url ?: api.getVideoStreamUrl(itemId),
                    playSessionId = playSessionId,
                    mimeType = if (isAudio) "audio/*" else "video/*"
                )
            } else {
                PlaybackConfig(url = api.getVideoStreamUrl(itemId), playSessionId = null, mimeType = "video/*")
            }
        } catch (e: Exception) {
            PlaybackConfig(url = api.getVideoStreamUrl(itemId), playSessionId = null, mimeType = "video/*")
        }
    }

    override suspend fun getItemDetails(itemId: String, userId: String): MediaItem = api.getItemDetails(itemId, userId)

    override suspend fun getPeople(itemId: String): List<Person> = api.getPeople(itemId)

    override suspend fun getSeasons(seriesId: String): List<MediaItem> = api.getSeasons(seriesId)

    override suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem> = api.getEpisodes(seriesId, seasonId)

    override fun getBaseUrl(): String = api.getBaseUrl()

    override fun getAccessToken(): String? = api.accessToken

    override suspend fun getHomeCache(): HomeContent? = withContext(Dispatchers.Default) {
        sessionManager.homeCache?.let {
            try { json.decodeFromString(HomeContent.serializer(), it) } catch (e: Exception) { null }
        }
    }

    override suspend fun saveHomeCache(content: HomeContent) = withContext(Dispatchers.Default) {
        sessionManager.homeCache = json.encodeToString(HomeContent.serializer(), content)
    }

    override suspend fun getMoviesCache(): List<MediaItem>? = withContext(Dispatchers.Default) {
        sessionManager.moviesCache?.let {
            try { json.decodeFromString(ListSerializer(MediaItem.serializer()), it) } catch (e: Exception) { null }
        }
    }

    override suspend fun saveMoviesCache(items: List<MediaItem>) = withContext(Dispatchers.Default) {
        sessionManager.moviesCache = json.encodeToString(ListSerializer(MediaItem.serializer()), items)
    }

    override suspend fun getTvShowsCache(): List<MediaItem>? = withContext(Dispatchers.Default) {
        sessionManager.tvShowsCache?.let {
            try { json.decodeFromString(ListSerializer(MediaItem.serializer()), it) } catch (e: Exception) { null }
        }
    }

    override suspend fun saveTvShowsCache(items: List<MediaItem>) = withContext(Dispatchers.Default) {
        sessionManager.tvShowsCache = json.encodeToString(ListSerializer(MediaItem.serializer()), items)
    }

    override suspend fun reportPlaybackStart(itemId: String, playSessionId: String) {
        api.reportPlaybackStart(itemId, playSessionId)
    }

    override suspend fun reportPlaybackProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean
    ) {
        api.reportPlaybackProgress(itemId, playSessionId, positionTicks, isPaused, isMuted)
    }

    override suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long) {
        api.reportPlaybackStopped(itemId, playSessionId, positionTicks)
    }
}
