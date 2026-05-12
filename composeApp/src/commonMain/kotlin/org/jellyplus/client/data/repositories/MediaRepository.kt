package org.jellyplus.client.data.repositories

import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.jellyplus.client.data.datasource.local.MediaLocalDataSource
import org.jellyplus.client.data.datasource.remote.IMediaRemoteDataSource
import org.jellyplus.client.data.remote.models.ChapterInfo
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.HomeContent
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.domain.models.Person
import org.jellyplus.client.domain.models.PlaybackConfig
import org.jellyplus.client.domain.repositories.IMediaRepository
import org.jellyplus.client.domain.repositories.ISessionRepository

class MediaRepository(
    private val remoteDataSource: IMediaRemoteDataSource,
    private val localDataSource: MediaLocalDataSource,
    private val sessionRepository: ISessionRepository,
    private val dispatchers: AppDispatchers,
) : IMediaRepository {

    override val movies: StateFlow<List<MediaItem>?> = localDataSource.movies
    override val tvShows: StateFlow<List<MediaItem>?> = localDataSource.tvShows
    override val homeContent: StateFlow<HomeContent?> = localDataSource.homeContent

    override suspend fun refreshMovies(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            val movies = remoteDataSource.getMovies()
            localDataSource.saveMoviesCache(movies)
        }
    }

    override suspend fun refreshTvShows(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            val shows = remoteDataSource.getTvShows()
            localDataSource.saveTvShowsCache(shows)
        }
    }

    override suspend fun refreshHomeContent(userId: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            val resume = remoteDataSource.getHomeResume(userId)
            val recentlyAdded = remoteDataSource.getHomeRecentlyAdded(userId)
            localDataSource.saveHomeCache(HomeContent(resume, recentlyAdded))
        }
    }

    override suspend fun searchItems(query: String): List<MediaItem> = withContext(dispatchers.io) {
        remoteDataSource.searchItems(query)
    }

    override suspend fun resolveStreamConfig(item: MediaItem, userId: String, deviceId: String): PlaybackConfig? = withContext(dispatchers.io) {
        val itemId = item.id

        try {
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

    override suspend fun getItemDetails(itemId: String, userId: String): MediaItem = withContext(dispatchers.io) {
        remoteDataSource.getItemDetails(itemId, userId)
    }

    override suspend fun getPeople(itemId: String): List<Person> = withContext(dispatchers.io) {
        remoteDataSource.getPeople(itemId)
    }

    override suspend fun getSeasons(seriesId: String): List<MediaItem> = withContext(dispatchers.io) {
        remoteDataSource.getSeasons(seriesId)
    }

    override suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem> = withContext(dispatchers.io) {
        remoteDataSource.getEpisodes(seriesId, seasonId)
    }

    override fun getBaseUrl(): String = remoteDataSource.getBaseUrl()

    override fun getAccessToken(): String? = sessionRepository.accessToken

    override fun clearCache() {
        localDataSource.clear()
    }

    override suspend fun reportPlaybackStart(itemId: String, playSessionId: String) = withContext(dispatchers.io) {
        remoteDataSource.reportPlaybackStart(itemId, playSessionId)
    }

    override suspend fun reportPlaybackProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean
    ) = withContext(dispatchers.io) {
        remoteDataSource.reportPlaybackProgress(itemId, playSessionId, positionTicks, isPaused, isMuted)
    }

    override suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long) = withContext(dispatchers.io) {
        remoteDataSource.reportPlaybackStopped(itemId, playSessionId, positionTicks)
    }

    override suspend fun markItemAsPlayed(userId: String, itemId: String) = withContext(dispatchers.io) {
        remoteDataSource.markAsPlayed(userId, itemId)
    }

    override suspend fun saveCustomMarker(itemId: String, startTicks: Long, endTicks: Long) = withContext(dispatchers.io) {
        val chapters = listOf(
            ChapterInfo(startPositionTicks = startTicks, name = "Custom_Preview_Marker_Start"),
            ChapterInfo(startPositionTicks = endTicks, name = "Custom_Preview_Marker_End"),
        )
        remoteDataSource.saveChapterMarker(itemId, chapters)
    }
}
