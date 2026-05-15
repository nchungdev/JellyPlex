package org.jellyplus.client.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jellyplus.client.domain.models.MediaItem

@Serializable
data class AuthenticateByNameRequest(
    @SerialName("Username") val username: String? = null,
    @SerialName("Pw") val pw: String? = null,
)

@Serializable
data class UpdateUserPasswordRequest(
    @SerialName("CurrentPw") val currentPassword: String,
    @SerialName("NewPw") val newPassword: String,
    @SerialName("ResetPassword") val resetPassword: Boolean = false,
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
data class MediaStreamDto(
    @SerialName("Type") val type: String = "",          // "Audio" | "Video" | "Subtitle"
    @SerialName("Language") val language: String? = null,
    @SerialName("IsDefault") val isDefault: Boolean = false,
    @SerialName("Index") val index: Int = 0,
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
    @SerialName("MediaStreams") val mediaStreams: List<MediaStreamDto> = emptyList(),
)

@Serializable
data class ItemResponse(
    @SerialName("Items") val items: List<MediaItem>,
    @SerialName("TotalRecordCount") val totalRecordCount: Int,
)

@Serializable
data class ChapterInfo(
    @SerialName("StartPositionTicks") val startPositionTicks: Long,
    @SerialName("Name") val name: String,
)

@Serializable
data class EpisodeChapterResponse(
    @SerialName("Chapters") val chapters: List<ChapterInfo> = emptyList(),
    @SerialName("RunTimeTicks") val runTimeTicks: Long? = null,
)

// Native Jellyfin MediaSegments API (v10.9+)
@Serializable
data class MediaSegmentDto(
    @SerialName("ItemId") val itemId: String = "",
    @SerialName("Type") val type: String = "Unknown",   // Intro | Outro | Recap | Preview | Commercial | Unknown
    @SerialName("StartTicks") val startTicks: Long = 0L,
    @SerialName("EndTicks") val endTicks: Long = 0L,
)

@Serializable
data class MediaSegmentQueryResult(
    @SerialName("Items") val items: List<MediaSegmentDto> = emptyList(),
    @SerialName("TotalRecordCount") val totalRecordCount: Int = 0,
)

// Intro Skipper plugin proprietary API
@Serializable
data class IntroSkipperSegment(
    @SerialName("Start") val start: Double = 0.0,
    @SerialName("End") val end: Double = 0.0,
    @SerialName("Valid") val valid: Boolean = false,
)

@Serializable
data class IntroSkipperTimestamps(
    @SerialName("Introduction") val introduction: IntroSkipperSegment? = null,
    @SerialName("Credits") val credits: IntroSkipperSegment? = null,
    @SerialName("Recap") val recap: IntroSkipperSegment? = null,
    @SerialName("Preview") val preview: IntroSkipperSegment? = null,
    @SerialName("Commercial") val commercial: IntroSkipperSegment? = null,
)

@Serializable
data class PlaybackStartInfo(
    @SerialName("ItemId") val itemId: String,
    @SerialName("PlaySessionId") val playSessionId: String,
    @SerialName("CanSeek") val canSeek: Boolean = true,
    @SerialName("IsPaused") val isPaused: Boolean = false,
    @SerialName("IsMuted") val isMuted: Boolean = false,
)

@Serializable
data class PlaybackProgressInfo(
    @SerialName("ItemId") val itemId: String,
    @SerialName("PlaySessionId") val playSessionId: String,
    @SerialName("PositionTicks") val positionTicks: Long,
    @SerialName("IsPaused") val isPaused: Boolean = false,
    @SerialName("IsMuted") val isMuted: Boolean = false,
)

@Serializable
data class PlaybackStopInfo(
    @SerialName("ItemId") val itemId: String,
    @SerialName("PlaySessionId") val playSessionId: String,
    @SerialName("PositionTicks") val positionTicks: Long,
)
