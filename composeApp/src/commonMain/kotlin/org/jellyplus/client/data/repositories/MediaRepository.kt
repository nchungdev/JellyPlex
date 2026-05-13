package org.jellyplus.client.data.repositories

import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.jellyplus.client.data.datasource.local.MediaLocalDataSource
import org.jellyplus.client.data.datasource.remote.IMediaRemoteDataSource
import org.jellyplus.client.data.remote.models.ChapterInfo
import org.jellyplus.client.data.remote.models.IntroSkipperSegment
import org.jellyplus.client.domain.models.AppDispatchers
import org.jellyplus.client.domain.models.HomeContent
import org.jellyplus.client.domain.models.IntroMarker
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
            val featured = runCatching { remoteDataSource.getHomeFeatured(userId) }.getOrDefault(emptyList())
            val resume = remoteDataSource.getHomeResume(userId)
            val recentlyAdded = remoteDataSource.getHomeRecentlyAdded(userId)
            localDataSource.saveHomeCache(HomeContent(featured, resume, recentlyAdded))
        }
    }

    override suspend fun getWatchHistory(userId: String): List<MediaItem> = withContext(dispatchers.io) {
        remoteDataSource.getWatchHistory(userId)
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
                val originalAudioLanguage = source.mediaStreams
                    .filter { it.type.equals("Audio", ignoreCase = true) && !it.language.isNullOrBlank() }
                    .firstOrNull { it.isDefault }
                    ?.language
                    ?.trim()

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
                    mimeType = if (isAudio) "audio/*" else "video/*",
                    originalAudioLanguage = if (isAudio) null else originalAudioLanguage,
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

    override suspend fun getIntroMarkers(itemId: String): List<IntroMarker> = withContext(dispatchers.io) {
        // 1. Native Jellyfin MediaSegments API (plugin-agnostic, preferred)
        try {
            val segments = remoteDataSource.getMediaSegments(itemId).items
                .filter { it.startTicks < it.endTicks }
            if (segments.isNotEmpty()) {
                return@withContext segments.mapNotNull { seg ->
                    val type = when (seg.type) {
                        "Intro" -> null
                        "Outro", "Credits" -> "Credits"
                        "Preview", "Commercial" -> "Preview"
                        else -> return@mapNotNull null  // Recap, Unknown — not auto-skipped
                    }
                    IntroMarker(startTicks = seg.startTicks, endTicks = seg.endTicks, type = type)
                }
            }
        } catch (_: Exception) {}

        // 2. Intro Skipper proprietary endpoint (/Episode/{id}/Timestamps)
        try {
            val ts = remoteDataSource.getIntroSkipperTimestamps(itemId)
            val markers = buildList {
                fun add(seg: IntroSkipperSegment?, type: String?) {
                    if (seg?.valid == true && seg.end > seg.start) {
                        val startTicks = (seg.start * 10_000_000).toLong()
                        val endTicks = (seg.end * 10_000_000).toLong()
                        add(IntroMarker(startTicks = startTicks, endTicks = endTicks, type = type))
                    }
                }
                add(ts.introduction, null)
                add(ts.credits, "Credits")
                add(ts.recap, null)
                add(ts.preview, "Preview")
                add(ts.commercial, "Preview")
            }
            if (markers.isNotEmpty()) return@withContext markers
        } catch (_: Exception) {}

        // 3. Chapters fallback (SkipMe.db and manual chapter tagging)
        try {
            val response = remoteDataSource.getEpisodeChapters(itemId)
            val chapters = response.chapters
            val runTimeTicks = response.runTimeTicks ?: 0L
            return@withContext buildList {
                chapters.forEachIndexed { index, chapter ->
                    val name = chapter.name.trim().lowercase()
                    val nextStart = chapters.getOrNull(index + 1)?.startPositionTicks ?: runTimeTicks
                    if (nextStart <= chapter.startPositionTicks) return@forEachIndexed
                    when {
                        name == "op" || name == "opening" || name == "intro" ->
                            add(IntroMarker(startTicks = chapter.startPositionTicks, endTicks = nextStart, type = null))
                        name == "ed" || name == "ending" || name == "outro" || name == "credits" ->
                            add(IntroMarker(startTicks = chapter.startPositionTicks, endTicks = runTimeTicks.coerceAtLeast(nextStart), type = "Credits"))
                    }
                }
            }
        } catch (_: Exception) {}

        emptyList()
    }
}
