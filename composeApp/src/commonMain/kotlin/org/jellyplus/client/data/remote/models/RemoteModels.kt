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
