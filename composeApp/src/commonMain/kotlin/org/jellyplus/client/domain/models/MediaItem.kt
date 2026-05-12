package org.jellyplus.client.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    @SerialName("Id") val id: String,
    @SerialName("Name") val title: String,
    @SerialName("Type") val type: MediaType = MediaType.MOVIE,
    // "Movie", "Series", "Season", "Episode"
    @SerialName("ImageTags") val imageTags: Map<String, String>? = null,
    @SerialName("RunTimeTicks") val runTimeTicks: Long? = null,
    @SerialName("Overview") val overview: String? = null,
    @SerialName("ProductionYear") val year: Int? = null,
    @SerialName("IndexNumber") val index: Int? = null,
    @SerialName("Genres") val genres: List<String>? = null,
    @SerialName("CommunityRating") val rating: Float? = null,
    @SerialName("People") val people: List<Person>? = null,
    @SerialName("BackdropImageTags") val backdropImageTags: List<String>? = null,
    @SerialName("ParentBackdropImageTags") val parentBackdropImageTags: List<String>? = null,
    @SerialName("ParentBackdropItemId") val parentBackdropItemId: String? = null,
    @SerialName("SeriesId") val seriesId: String? = null,
    @SerialName("SeasonId") val seasonId: String? = null,
    @SerialName("ParentIndexNumber") val parentIndexNumber: Int? = null,
) {
    fun getImageUrl(baseUrl: String): String? {
        return if (imageTags?.containsKey("Primary") == true) {
            "$baseUrl/Items/$id/Images/Primary"
        } else {
            null
        }
    }

    fun getBackdropUrl(baseUrl: String): String? {
        return when {
            !backdropImageTags.isNullOrEmpty() -> "$baseUrl/Items/$id/Images/Backdrop"
            !parentBackdropImageTags.isNullOrEmpty() && parentBackdropItemId != null ->
                "$baseUrl/Items/$parentBackdropItemId/Images/Backdrop"

            imageTags?.containsKey("Backdrop") == true -> "$baseUrl/Items/$id/Images/Backdrop"
            else -> getImageUrl(baseUrl)
        }
    }
}

@Serializable
data class Person(
    @SerialName("Name") val name: String,
    @SerialName("Id") val id: String,
    @SerialName("Role") val role: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("PrimaryImageTag") val primaryImageTag: String? = null,
) {
    fun getImageUrl(baseUrl: String): String? {
        return if (primaryImageTag != null) {
            "$baseUrl/Items/$id/Images/Primary"
        } else {
            null
        }
    }
}
