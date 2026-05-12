package org.jellyplus.client.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jellyplus.client.data.remote.models.*
import org.jellyplus.client.domain.models.*
import org.jellyplus.client.domain.repositories.ISessionRepository

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class JellyfinApi(
    private val sessionRepository: ISessionRepository,
    var onSessionExpired: (() -> Unit)? = null,
) {
    // Dynamic properties to ensure we always use the latest session data
    val baseUrl: String get() = sessionRepository.baseUrl ?: ""
    val accessToken: String? get() = sessionRepository.accessToken

    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        accessToken?.let { BearerTokens(it, "") }
                    }
                    refreshTokens {
                        val username = sessionRepository.userName
                        val password = sessionRepository.password
                        if (username != null && password != null) {
                            try {
                                val result = silentLogin(username, password)
                                // Update session via repository
                                result.accessToken?.let {
                                    sessionRepository.updateToken(it)
                                }
                                BearerTokens(result.accessToken ?: "", "")
                            } catch (e: Exception) {
                                println("JellyfinApi: Token refresh failed - ${e.message}")
                                onSessionExpired?.invoke()
                                null
                            }
                        } else {
                            onSessionExpired?.invoke()
                            null
                        }
                    }
                    sendWithoutRequest { request: HttpRequestBuilder ->
                        val path = request.url.encodedPath
                        path.contains("/Users/AuthenticateByName") ||
                            path.contains("/QuickConnect/Connect") ||
                            path.contains("/QuickConnect/Initiate") ||
                            path.contains("/System/Info/Public")
                    }
                }
            }
            expectSuccess = true
        }

    private fun HttpRequestBuilder.apiUrl(vararg segments: String) {
        url {
            takeFrom(this@JellyfinApi.baseUrl)
            appendPathSegments(segments.toList())
        }
        header("X-Emby-Authorization", authHeader())
    }

    fun buildUrl(vararg segments: String, block: URLBuilder.() -> Unit = {}): String {
        val currentBaseUrl = baseUrl
        if (currentBaseUrl.isEmpty()) return ""

        return URLBuilder(currentBaseUrl).apply {
            appendPathSegments(segments.toList())
            block()
            // Append ApiKey for media URLs if it's not already in the query
            accessToken?.let { token ->
                if (!parameters.contains("ApiKey")) {
                    parameters.append("ApiKey", token)
                }
            }
        }.buildString()
    }

    private suspend fun silentLogin(username: String, password: String): AuthenticationResult {
        val loginClient = HttpClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val result = loginClient.post {
            url {
                takeFrom(this@JellyfinApi.baseUrl)
                appendPathSegments("Users", "AuthenticateByName")
            }
            header("X-Emby-Authorization", authHeader(includeToken = false))
            contentType(ContentType.Application.Json)
            setBody(org.jellyplus.client.data.remote.models.AuthenticateByNameRequest(username, password))
        }.body<AuthenticationResult>()
        loginClient.close()
        return result
    }

    private fun formatUrl(url: String): String {
        var formattedUrl = url.trim()
        if (formattedUrl.isEmpty()) return ""

        if (!formattedUrl.startsWith("http")) {
            formattedUrl = if (formattedUrl.contains(Constants.DEMO_SERVER_HOST)) {
                "https://$formattedUrl"
            } else {
                "http://$formattedUrl"
            }
        }

        formattedUrl = formattedUrl.removeSuffix("/")

        val protocolSeparatorIndex = formattedUrl.indexOf("://")
        val urlWithoutProtocol = if (protocolSeparatorIndex != -1) {
            formattedUrl.substring(protocolSeparatorIndex + 3)
        } else {
            formattedUrl
        }

        val isDemo = urlWithoutProtocol.contains(Constants.DEMO_SERVER_HOST)
        val hasPath = urlWithoutProtocol.contains("/")
        val hasPort = urlWithoutProtocol.contains(":")

        if (!hasPort && !hasPath && !isDemo) {
            formattedUrl = "$formattedUrl:8096"
        }

        return formattedUrl
    }

    fun updateBaseUrl(newUrl: String) {
        val formatted = formatUrl(newUrl)
        if (formatted.isNotEmpty()) {
            sessionRepository.updateBaseUrl(formatted)
        }
    }

    suspend fun validateServer(url: String? = null): Boolean {
        return try {
            val info = getPublicSystemInfo(url?.let { formatUrl(it) })
            !info.id.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun getVideoStreamUrl(itemId: String): String {
        return buildUrl("videos", itemId, "stream") {
            parameters.append("Static", "true")
        }
    }

    private fun authHeader(includeToken: Boolean = true): String {
        val deviceName = sessionRepository.deviceName
        val deviceId = sessionRepository.deviceId
        var header = "MediaBrowser Client=\"JellyPlus\", Device=\"$deviceName\", DeviceId=\"$deviceId\", Version=\"1.0.0\""
        if (includeToken) {
            accessToken?.let { header += ", Token=\"$it\"" }
        }
        return header
    }

    suspend fun getPublicSystemInfo(url: String? = null): PublicSystemInfo {
        return client.get {
            if (url != null) {
                this.url.takeFrom(url)
                this.url.appendPathSegments("System", "Info", "Public")
            } else {
                apiUrl("System", "Info", "Public")
            }
        }.body()
    }

    suspend fun getCurrentUser(): UserDto {
        return client.get {
            apiUrl("Users", "Me")
        }.body()
    }

    suspend fun initiateQuickConnect(): QuickConnectResult {
        val response = client.post {
            apiUrl("QuickConnect", "Initiate")
            header("X-Emby-Authorization", authHeader(includeToken = false))
        }
        return json.decodeFromString(QuickConnectResult.serializer(), response.bodyAsText())
    }

    suspend fun getQuickConnectState(secret: String): QuickConnectResult {
        val response = client.get {
            apiUrl("QuickConnect", "Connect")
            parameter("secret", secret)
            header("X-Emby-Authorization", authHeader(includeToken = false))
        }
        return json.decodeFromString(QuickConnectResult.serializer(), response.bodyAsText())
    }

    suspend fun authenticateWithQuickConnect(secret: String): AuthenticationResult {
        return client.post {
            apiUrl("Users", "AuthenticateWithQuickConnect")
            header("X-Emby-Authorization", authHeader(includeToken = false))
            contentType(ContentType.Application.Json)
            setBody(mapOf("Secret" to secret))
        }.body()
    }

    suspend fun authenticateByName(request: org.jellyplus.client.data.remote.models.AuthenticateByNameRequest): AuthenticationResult {
        return client.post {
            apiUrl("Users", "AuthenticateByName")
            header("X-Emby-Authorization", authHeader(includeToken = false))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getItemDetails(itemId: String, userId: String): MediaItem {
        return client.get {
            apiUrl("Users", userId, "Items", itemId)
            parameter("Fields", "PrimaryImageAspectRatio,CanDelete,Overview,Genres,CommunityRating,People,RunTimeTicks")
        }.body()
    }

    suspend fun getPeople(itemId: String): List<Person> {
        val item = getItemDetails(itemId, "")
        return item.people ?: emptyList()
    }

    suspend fun searchItems(query: String): List<MediaItem> {
        val response: ItemResponse = client.get {
            apiUrl("Items")
            parameter("searchTerm", query)
            parameter("Recursive", true)
            parameter("IncludeItemTypes", "${MediaType.MOVIE.value},${MediaType.SERIES.value}")
            parameter("Fields", "PrimaryImageAspectRatio,CanDelete")
        }.body()
        return response.items
    }

    suspend fun reportPlaybackStart(itemId: String, playSessionId: String) {
        val body = PlaybackStartInfo(itemId = itemId, playSessionId = playSessionId)
        android.util.Log.d("JellyfinApi", "reportPlaybackStart → POST $baseUrl/Sessions/Playing | body=$body")
        try {
            val response = client.post {
                apiUrl("Sessions", "Playing")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            android.util.Log.d("JellyfinApi", "reportPlaybackStart ← ${response.status}")
        } catch (e: Exception) {
            android.util.Log.e("JellyfinApi", "reportPlaybackStart FAILED: ${e.message}", e)
            throw e
        }
    }

    suspend fun reportPlaybackProgress(itemId: String, playSessionId: String, positionTicks: Long, isPaused: Boolean, isMuted: Boolean = false) {
        val body = PlaybackProgressInfo(itemId = itemId, playSessionId = playSessionId, positionTicks = positionTicks, isPaused = isPaused, isMuted = isMuted)
        android.util.Log.d("JellyfinApi", "reportPlaybackProgress → POST $baseUrl/Sessions/Playing/Progress | body=$body")
        try {
            val response = client.post {
                apiUrl("Sessions", "Playing", "Progress")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            android.util.Log.d("JellyfinApi", "reportPlaybackProgress ← ${response.status}")
        } catch (e: Exception) {
            android.util.Log.e("JellyfinApi", "reportPlaybackProgress FAILED: ${e.message}", e)
            throw e
        }
    }

    suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long) {
        val body = PlaybackStopInfo(itemId = itemId, playSessionId = playSessionId, positionTicks = positionTicks)
        android.util.Log.d("JellyfinApi", "reportPlaybackStopped → POST $baseUrl/Sessions/Playing/Stopped | body=$body")
        try {
            val response = client.post {
                apiUrl("Sessions", "Playing", "Stopped")
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            android.util.Log.d("JellyfinApi", "reportPlaybackStopped ← ${response.status}")
        } catch (e: Exception) {
            android.util.Log.e("JellyfinApi", "reportPlaybackStopped FAILED: ${e.message}", e)
            throw e
        }
    }

    suspend fun getWatchHistory(userId: String): List<MediaItem> {
        val response: ItemResponse = client.get {
            apiUrl("Users", userId, "Items")
            parameter("SortBy", "DatePlayed")
            parameter("SortOrder", "Descending")
            parameter("Filters", "IsPlayed")
            parameter("Recursive", true)
            parameter("IncludeItemTypes", "${MediaType.MOVIE.value},${MediaType.EPISODE.value}")
            parameter("Fields", "UserData,Overview,PrimaryImageAspectRatio,RunTimeTicks,SeriesName,ParentIndexNumber,IndexNumber")
            parameter("Limit", 50)
        }.body()
        return response.items
    }

    suspend fun getResumeItems(userId: String): List<MediaItem> {
        val response: ItemResponse = client.get {
            apiUrl("Users", userId, "Items", "Resume")
            parameter("Fields", "PrimaryImageAspectRatio,CanDelete")
        }.body()
        return response.items
    }

    suspend fun getRecentlyAddedItems(userId: String): List<MediaItem> {
        return client.get {
            apiUrl("Users", userId, "Items", "Latest")
            parameter("Limit", 20)
            parameter("Fields", "PrimaryImageAspectRatio,CanDelete")
        }.body()
    }

    suspend fun getMovies(): List<MediaItem> {
        val response: ItemResponse = client.get {
            apiUrl("Items")
            parameter("SortBy", "SortName")
            parameter("SortOrder", "Ascending")
            parameter("Recursive", true)
            parameter("IncludeItemTypes", MediaType.MOVIE.value)
            parameter("Fields", "PrimaryImageAspectRatio,CanDelete")
        }.body()
        return response.items
    }

    suspend fun getTvShows(): List<MediaItem> {
        val response: ItemResponse = client.get {
            apiUrl("Items")
            parameter("SortBy", "SortName")
            parameter("SortOrder", "Ascending")
            parameter("Recursive", true)
            parameter("IncludeItemTypes", MediaType.SERIES.value)
            parameter("Fields", "PrimaryImageAspectRatio,CanDelete")
        }.body()
        return response.items
    }

    suspend fun getSeasons(seriesId: String): List<MediaItem> {
        val response: ItemResponse = client.get {
            apiUrl("Shows", seriesId, "Seasons")
            parameter("Fields", "Overview,ImageTags,UserData")
        }.body()
        return response.items
    }

    suspend fun getEpisodes(seriesId: String, seasonId: String): List<MediaItem> {
        val response: ItemResponse = client.get {
            apiUrl("Shows", seriesId, "Episodes")
            parameter("seasonId", seasonId)
            parameter("Fields", "Overview,RunTimeTicks,ImageTags,UserData")
        }.body()
        return response.items
    }

    suspend fun markAsPlayed(userId: String, itemId: String) {
        client.post { apiUrl("Users", userId, "PlayedItems", itemId) }
    }

    suspend fun saveChapterMarker(itemId: String, chapters: List<ChapterInfo>) {
        client.post {
            apiUrl("Items", itemId, "Chapters")
            contentType(ContentType.Application.Json)
            setBody(chapters)
        }
    }

    suspend fun getPlaybackInfo(itemId: String, userId: String, deviceProfile: DeviceProfile? = null): PlaybackInfoResponse {
        return client.post {
            apiUrl("Items", itemId, "PlaybackInfo")
            parameter("userId", userId)
            parameter("enableDirectPlay", false)
            parameter("enableDirectStream", false)
            contentType(ContentType.Application.Json)
            setBody(deviceProfile ?: DefaultDeviceProfile)
        }.body()
    }
}

val DefaultDeviceProfile = DeviceProfile(
    directPlayProfiles = listOf(
        DirectPlayProfile("mp4,m4v", "Video", "h264,hevc,vp9,av1", "aac,mp3,opus,flac,vorbis"),
        DirectPlayProfile("mkv", "Video", "h264,hevc,vp9,av1", "aac,mp3,opus,flac,vorbis"),
        DirectPlayProfile("mp3", "Audio"),
        DirectPlayProfile("aac", "Audio"),
        DirectPlayProfile("flac", "Audio"),
        DirectPlayProfile("opus", "Audio"),
        DirectPlayProfile("wav", "Audio")
    ),
    transcodingProfiles = listOf(
        TranscodingProfile("ts", "Video", "h264", "aac", "hls", "Streaming"),
        TranscodingProfile("mp3", "Audio", "", "mp3", "http", "Streaming")
    ),
    subtitleProfiles = listOf(
        SubtitleProfile("srt", "Embed"),
        SubtitleProfile("vtt", "Embed")
    )
)
