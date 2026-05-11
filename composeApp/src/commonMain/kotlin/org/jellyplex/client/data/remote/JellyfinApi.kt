package org.jellyplex.client.data.remote

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
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jellyplex.client.data.local.SessionManager
import org.jellyplex.client.domain.models.*

@Serializable
data class AuthenticateByNameRequest(
    @SerialName("Username") val username: String? = null,
    @SerialName("Pw") val pw: String? = null,
)

class JellyfinApi(
    private val sessionManager: SessionManager? = null,
    var onSessionExpired: (() -> Unit)? = null,
) {
    private var baseUrl: String = sessionManager?.baseUrl ?: ""

    var accessToken: String?
        get() = sessionManager?.accessToken
        set(value) {
            sessionManager?.accessToken = value
        }

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
                        sessionManager?.accessToken?.let { BearerTokens(it, "") }
                    }
                    refreshTokens {
                        val username = sessionManager?.userName
                        val password = sessionManager?.password
                        if (username != null && password != null) {
                            try {
                                val result = silentLogin(username, password)
                                sessionManager.accessToken = result.accessToken
                                BearerTokens(result.accessToken ?: "", "")
                            } catch (e: Exception) {
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
                            path.contains("/System/Info/Public")
                    }
                }
            }
            expectSuccess = true
        }

    /**
     * Helper to configure the API URL and common headers for a request.
     */
    private fun HttpRequestBuilder.apiUrl(vararg segments: String) {
        url {
            takeFrom(this@JellyfinApi.baseUrl)
            appendPathSegments(segments.toList())
        }
        header("X-Emby-Authorization", authHeader())
    }

    /**
     * Builds a full URL string for public or media access (e.g. streaming).
     * Automatically appends ApiKey if available and not already present.
     */
    fun buildUrl(vararg segments: String, block: URLBuilder.() -> Unit = {}): String {
        return URLBuilder(baseUrl).apply {
            appendPathSegments(segments.toList())
            block()
            // Append ApiKey for media URLs if it's not already in the query
            if (accessToken != null && !parameters.contains("ApiKey")) {
                parameters.append("ApiKey", accessToken!!)
            }
        }.buildString()
    }

    private suspend fun silentLogin(username: String, password: String): AuthenticationResult {
        // Use a separate client or bypass Auth to avoid recursion
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
            setBody(AuthenticateByNameRequest(username, password))
        }.body<AuthenticationResult>()
        loginClient.close()
        return result
    }

    private fun formatUrl(url: String): String {
        var formattedUrl = url.trim()
        if (formattedUrl.isEmpty()) return ""

        if (!formattedUrl.startsWith("http")) {
            // Force https for official demo
            formattedUrl = if (formattedUrl.contains("demo.jellyfin.org")) {
                "https://$formattedUrl"
            } else {
                "http://$formattedUrl"
            }
        }

        // Remove trailing slash
        formattedUrl = formattedUrl.removeSuffix("/")

        // Automatically add port 8096 if no port is specified and it's not a common port
        val protocolSeparatorIndex = formattedUrl.indexOf("://")
        val urlWithoutProtocol = if (protocolSeparatorIndex != -1) {
            formattedUrl.substring(protocolSeparatorIndex + 3)
        } else {
            formattedUrl
        }

        // Only append 8096 if:
        // 1. No port is explicitly specified
        // 2. No path is present (it's just a host)
        // 3. It's not the official demo domain (which uses standard 443 via reverse proxy)
        val isDemo = urlWithoutProtocol.contains("demo.jellyfin.org")
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
            baseUrl = formatted
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

    fun getBaseUrl(): String = baseUrl

    fun getVideoStreamUrl(itemId: String): String {
        return buildUrl("videos", itemId, "stream") {
            parameters.append("Static", "true")
        }
    }

    fun getStreamUrl(
        itemId: String,
        token: String,
    ): String {
        return buildUrl("videos", itemId, "stream") {
            parameters.append("Static", "true")
            // Override ApiKey if a specific token is provided
            parameters["ApiKey"] = token
        }
    }

    private fun authHeader(includeToken: Boolean = true): String {
        var header =
            "MediaBrowser Client=\"JellyPlex\", Device=\"CMP-Device\", " +
                "DeviceId=\"CMP-ID\", Version=\"1.0.0\""
        if (includeToken) {
            accessToken?.let {
                header += ", Token=\"$it\""
            }
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
        return client.post {
            apiUrl("QuickConnect", "Initiate")
        }.body()
    }

    suspend fun getQuickConnectState(secret: String): QuickConnectResult {
        return client.get {
            apiUrl("QuickConnect", "Connect")
            parameter("secret", secret)
        }.body()
    }

    suspend fun authenticateByName(request: AuthenticateByNameRequest): AuthenticationResult {
        return client.post {
            apiUrl("Users", "AuthenticateByName")
            header("X-Emby-Authorization", authHeader(includeToken = false))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getItemDetails(
        itemId: String,
        userId: String,
    ): MediaItem {
        return client.get {
            apiUrl("Users", userId, "Items", itemId)
            parameter("Fields", "PrimaryImageAspectRatio,CanDelete,Overview,Genres,CommunityRating,People,RunTimeTicks")
        }.body()
    }

    suspend fun getPeople(itemId: String): List<org.jellyplex.client.domain.models.Person> {
        val item = getItemDetails(itemId, "") // UserId is not strictly required for People
        return item.people ?: emptyList()
    }

    suspend fun searchItems(query: String): List<MediaItem> {
        val response: ItemResponse =
            client.get {
                apiUrl("Items")
                parameter("searchTerm", query)
                parameter("Recursive", true)
                parameter("IncludeItemTypes", "${MediaType.MOVIE.value},${MediaType.SERIES.value}")
                parameter("Fields", "PrimaryImageAspectRatio,CanDelete")
            }.body()
        return response.items
    }

    // Playback Reporting
    suspend fun reportPlaybackStart(itemId: String, playSessionId: String) {
        client.post {
            apiUrl("Sessions", "Playing")
            parameter("ItemId", itemId)
            parameter("PlaySessionId", playSessionId)
        }
    }

    suspend fun reportPlaybackProgress(
        itemId: String,
        playSessionId: String,
        positionTicks: Long,
        isPaused: Boolean,
        isMuted: Boolean = false
    ) {
        client.post {
            apiUrl("Sessions", "Playing", "Progress")
            parameter("ItemId", itemId)
            parameter("PlaySessionId", playSessionId)
            parameter("PositionTicks", positionTicks)
            parameter("IsPaused", isPaused)
            parameter("IsMuted", isMuted)
        }
    }

    suspend fun reportPlaybackStopped(itemId: String, playSessionId: String, positionTicks: Long) {
        client.post {
            apiUrl("Sessions", "Playing", "Stopped")
            parameter("ItemId", itemId)
            parameter("PlaySessionId", playSessionId)
            parameter("PositionTicks", positionTicks)
        }
    }

    suspend fun getResumeItems(userId: String): List<MediaItem> {
        val response: ItemResponse =
            client.get {
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
        val response: ItemResponse =
            client.get {
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
        val response: ItemResponse =
            client.get {
                apiUrl("Items")
                parameter("SortBy", "SortName")
                parameter("SortOrder", "Ascending")
                parameter("Recursive", true)
                parameter("IncludeItemTypes", MediaType.SERIES.value)
                parameter("Fields", "PrimaryImageAspectRatio,CanDelete")
            }.body()
        return response.items
    }

    suspend fun getIntroMarkers(itemId: String): List<IntroMarker> {
        return try {
            client.get {
                apiUrl("Items", itemId, "IntroMarkers")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSeasons(seriesId: String): List<MediaItem> {
        val response: ItemResponse =
            client.get {
                apiUrl("Shows", seriesId, "Seasons")
                parameter("Fields", "Overview,ImageTags")
            }.body()
        return response.items
    }

    suspend fun getEpisodes(
        seriesId: String,
        seasonId: String,
    ): List<MediaItem> {
        val response: ItemResponse =
            client.get {
                apiUrl("Shows", seriesId, "Episodes")
                parameter("seasonId", seasonId)
                parameter("Fields", "Overview,RunTimeTicks,ImageTags")
            }.body()
        return response.items
    }

    suspend fun getPlaybackInfo(
        itemId: String,
        userId: String,
        deviceProfile: DeviceProfile? = null,
    ): PlaybackInfoResponse {
        val response: PlaybackInfoResponse =
            client.post {
                apiUrl("Items", itemId, "PlaybackInfo")
                parameter("userId", userId)
                parameter("enableDirectPlay", false)
                parameter("enableDirectStream", false)
                contentType(ContentType.Application.Json)
                setBody(deviceProfile ?: DefaultDeviceProfile)
            }.body()
        return response
    }
}

val DefaultDeviceProfile =
    DeviceProfile(
        directPlayProfiles =
            listOf(
                DirectPlayProfile(
                    container = "mp4,m4v",
                    type = "Video",
                    videoCodec = "h264,hevc,vp9,av1",
                    audioCodec = "aac,mp3,opus,flac,vorbis",
                ),
                DirectPlayProfile(
                    container = "mkv",
                    type = "Video",
                    videoCodec = "h264,hevc,vp9,av1",
                    audioCodec = "aac,mp3,opus,flac,vorbis",
                ),
                DirectPlayProfile(container = "mp3", type = "Audio"),
                DirectPlayProfile(container = "aac", type = "Audio"),
                DirectPlayProfile(container = "flac", type = "Audio"),
                DirectPlayProfile(container = "opus", type = "Audio"),
                DirectPlayProfile(container = "wav", type = "Audio"),
            ),
        transcodingProfiles =
            listOf(
                TranscodingProfile(
                    container = "ts",
                    type = "Video",
                    audioCodec = "aac",
                    videoCodec = "h264",
                    protocol = "hls",
                    context = "Streaming",
                ),
                TranscodingProfile(
                    container = "mp3",
                    type = "Audio",
                    audioCodec = "mp3",
                    videoCodec = "",
                    protocol = "http",
                    context = "Streaming",
                ),
            ),
        subtitleProfiles =
            listOf(
                SubtitleProfile(format = "srt", method = "Embed"),
                SubtitleProfile(format = "vtt", method = "Embed"),
            ),
    )

@Serializable
data class DeviceProfile(
    @SerialName("MaxStreamingBitrate") val maxStreamingBitrate: Long = 140000000,
    @SerialName("MaxStaticBitrate") val maxStaticBitrate: Long = 140000000,
    @SerialName("MusicStreamingTranscodingBitrate") val musicStreamingTranscodingBitrate: Int = 192000,
    @SerialName("DirectPlayProfiles") val directPlayProfiles: List<DirectPlayProfile> = emptyList(),
    @SerialName("TranscodingProfiles") val transcodingProfiles: List<TranscodingProfile> = emptyList(),
    @SerialName("ContainerProfiles") val containerProfiles: List<ContainerProfile> = emptyList(),
    @SerialName("CodecProfiles") val codecProfiles: List<CodecProfile> = emptyList(),
    @SerialName("ResponseProfiles") val responseProfiles: List<ResponseProfile> = emptyList(),
    @SerialName("SubtitleProfiles") val subtitleProfiles: List<SubtitleProfile> = emptyList(),
)

@Serializable
data class DirectPlayProfile(
    @SerialName("Container") val container: String,
    @SerialName("Type") val type: String,
    @SerialName("VideoCodec") val videoCodec: String? = null,
    @SerialName("AudioCodec") val audioCodec: String? = null,
)

@Serializable
data class TranscodingProfile(
    @SerialName("Container") val container: String,
    @SerialName("Type") val type: String,
    @SerialName("VideoCodec") val videoCodec: String,
    @SerialName("AudioCodec") val audioCodec: String,
    @SerialName("Protocol") val protocol: String,
    @SerialName("Context") val context: String,
)

@Serializable
data class ContainerProfile(
    @SerialName("Type") val type: String,
)

@Serializable
data class CodecProfile(
    @SerialName("Type") val type: String,
)

@Serializable
data class ResponseProfile(
    @SerialName("Type") val type: String,
)

@Serializable
data class SubtitleProfile(
    @SerialName("Format") val format: String,
    @SerialName("Method") val method: String,
)

@Serializable
data class PlaybackInfoResponse(
    @SerialName("MediaSources") val mediaSources: List<MediaSource>,
    @SerialName("PlaySessionId") val playSessionId: String? = null,
)

@Serializable
data class MediaSource(
    @SerialName("Id") val id: String,
    @SerialName("SupportsDirectPlay") val supportsDirectPlay: Boolean = false,
    @SerialName("SupportsDirectStream") val supportsDirectStream: Boolean = false,
    @SerialName("SupportsTranscoding") val supportsTranscoding: Boolean = false,
    @SerialName("Container") val container: String? = null,
    @SerialName("Protocol") val protocol: String? = null,
    @SerialName("Path") val path: String? = null,
    @SerialName("IsRemote") val isRemote: Boolean = false,
    @SerialName("TranscodingUrl") val transcodingUrl: String? = null,
)

@Serializable
data class IntroMarker(
    @SerialName("StartPositionTicks") val startTicks: Long,
    @SerialName("EndPositionTicks") val endTicks: Long,
    @SerialName("Type") val type: String? = null,
)

@Serializable
data class ItemResponse(
    @SerialName("Items") val items: List<MediaItem>,
    @SerialName("TotalRecordCount") val totalRecordCount: Int,
)
