package org.jellyplus.client.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IntroMarker(
    @SerialName("StartPositionTicks") val startTicks: Long,
    @SerialName("EndPositionTicks") val endTicks: Long,
    @SerialName("Type") val type: String? = null,
)
