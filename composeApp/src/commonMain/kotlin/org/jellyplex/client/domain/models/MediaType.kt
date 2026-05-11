package org.jellyplex.client.domain.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MediaTypeSerializer::class)
enum class MediaType(val value: String) {
    MOVIE("Movie"),
    SERIES("Series"),
    SEASON("Season"),
    EPISODE("Episode"),
    AUDIO("Audio"),
    UNKNOWN("Unknown");

    companion object {
        fun fromString(value: String?): MediaType {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}

object MediaTypeSerializer : KSerializer<MediaType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MediaType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MediaType {
        return MediaType.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: MediaType) {
        encoder.encodeString(value.value)
    }
}
