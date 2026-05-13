package org.jellyplus.client.domain.models

data class PlaybackConfig(
    val url: String,
    val playSessionId: String?,
    val mimeType: String?,
    val originalAudioLanguage: String? = null,
)
