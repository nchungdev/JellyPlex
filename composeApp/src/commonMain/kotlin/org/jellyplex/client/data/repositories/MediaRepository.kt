package org.jellyplex.client.data.repositories

import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.data.datasource.local.IMediaLocalDataSource
import org.jellyplex.client.data.datasource.local.ISessionLocalDataSource
import org.jellyplex.client.data.datasource.remote.IMediaRemoteDataSource
import org.jellyplex.client.domain.models.*
import org.jellyplex.client.domain.repositories.IMediaRepository

class MediaRepository(
    private val remoteDataSource: IMediaRemoteDataSource,
    private val localDataSource: IMediaLocalDataSource,
    private val sessionDataSource: ISessionLocalDataSource,
    private val dispatchers: AppDispatchers,
) : IMediaRepository {

    override val movies: StateFlow<List<MediaItem>?> = localDataSource.movies
    override val tvShows: StateFlow<List<MediaItem>?> = localDataSource.tvShows
    override val homeContent: StateFlow<HomeContent?> = localDataSource.homeContent

    override suspend fun refreshMovies(): Result<Unit> = runCatching {
        val movies = remoteDataSource.getMovies()
        localDataSource.saveMoviesCache(movies)
    }

    override suspend fun refreshTvShows(): Result<Unit> = runCatching {
        val shows = remoteDataSource.getTvShows()
        localDataSource.saveTvShowsCache(shows)
    }

    override suspend fun refreshHomeContent(userId: String): Result<Unit> = runCatching {
        val resume = remoteDataSource.getHomeResume(userId)
        val recentlyAdded = remoteDataSource.getHomeRecentlyAdded(userId)
        localDataSource.saveHomeCache(HomeContent(resume, recentlyAdded))
    }

    override suspend fun searchItems(query: String): List<MediaItem> = remoteDataSource.searchItems(query)

    override suspend fun resolveStreamConfig(item: MediaItem, userId: String, deviceId: String): PlaybackConfig? {
        val itemId = item.id

        return try {
            val info = remoteDataSource.getPlaybackInfo(itemId, userId)
            val source = info.mediaSources.firstOrNull()
            val playSessionId = info.playSessionId

            if (source != null) {
                val isAudio = item.type == MediaType.AUDIO
                val typePath = if (isAudio) "audio" else "videos"

                val url = when {
                    source.isRemote && source.path?.startsWith("http") == true -> {
                        source.path
                    }
                    !source.transcodingUrl.isNullOrEmpty() -> {
                        if (source.transcodingUrl.startsWith("http")) {
                            source.transcodingUrl
                        } else {
                            remoteDataSource.buildUrl { takeFrom(source.transcodingUrl) }
                        }
                    }
                    source.supportsDirectPlay || source.supportsDirectStream -> {
                        val containerLower = source.container?.lowercase()
                        if (containerLower == "hls" || containerLower == "m3u8") {
                            remoteDataSource.buildUrl {
                                appendPathSegments(typePath, itemId, "master.m3u8")
                                parameters.append("MediaSourceId", source.id)
                                parameters.append("DeviceId", deviceId)
                            }
                        } else {
                            val ext = source.container?.let { ".$it" } ?: ""
                            remoteDataSource.buildUrl {
                                appendPathSegments(typePath, itemId, "stream$ext")
                                parameters.append("Static", "true")
                                parameters.append("MediaSourceId", source.id)
                                parameters.append("DeviceId", deviceId)
                            }
                        }
                    }

                    else -> remoteDataSource.getVideoStreamUrl(itemId)
                }
                PlaybackConfig(
                    url = url ?: remoteDataSource.getVideoStreamUrl(itemId),
                    playSessionId = playSessionId,
                    mimeType = if (isAudio) "audio/*" else "video/*"
                )
            } else {
                PlaybackConfig(url = remoteDataSource.getVideoStreamUrl(itemId), playSessionId = null, mimeType = "video/*")
            }
        } catch (e: Exception) {
            PlaybackConfig(url = remoteDataSource.getVideoStreamUrl(itemId), playSessionId = null, mimeType = "video/*")
        }
    }

    override suspend fun getItemDetails(itemId: String, userId: String): MediaItem = remoteDataSource.getItemDetails(itemId, userId)

    override suspend fun getPeople(itemId: String): List<Person> = remoteDataSource.getPeople(itemId)

    override suspend fun getSeasons(seriesId: String): List<MediaItem> = remoteDataSource.getSeasons(seriesId)

    override suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem> = remoteDataSource.getEpisodes(seriesId, seasonId)

    override fun getBaseUrl(): String = remoteDataSource.getBaseUrl()

    override fun getAccessToken(): String? = remoteDataSource.getAccessToken()

    override fun clearCache() {
        localDataSource.clear()
    }

    override suspend fun reportPlaybackStart(itemId: String, playSessionId: String) {
        remoteDataSource.reportPlaybackStart(itemId, playSessionId)
    }

    override suspend fun reportPlaybackProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean
    ) {
        remoteDataSource.reportPlaybackProgress(itemId, playSessionId, positionTicks, isPaused, isMuted)
    }

    override suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long) {
        remoteDataSource.reportPlaybackStopped(itemId, playSessionId, positionTicks)
    }
}
