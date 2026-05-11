package org.jellyplex.client.domain.models

data class PlaybackConfig(
    val url: String,
    val playSessionId: String?,
    val mimeType: String?
)
