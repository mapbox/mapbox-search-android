package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.SearchResultMetadata

internal data class SearchResultMetadataDAO(
    @SerializedName("metadata") val metadata: HashMap<String, String>? = null,
    @SerializedName("reviewCount") val reviewCount: Int? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("website") val website: String? = null,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("primaryPhotos") val primaryPhotos: List<ImageInfoDAO>? = null,
    @SerializedName("otherPhotos") val otherPhotos: List<ImageInfoDAO>? = null,
    @SerializedName("openHours") val openHours: OpenHoursDAO? = null,
    @SerializedName("parking") val parking: ParkingDataDAO? = null,
    @SerializedName("cpsJson") val cpsJson: String? = null,
    @SerializedName("rating") val rating: Float? = null,
) : DataAccessObject<SearchResultMetadata?> {

    override val isValid: Boolean
        get() = openHours?.isValid != false && parking?.isValid != false

    @OptIn(MapboxExperimental::class)
    override fun createData(): SearchResultMetadata? {
        val validPrimaryPhotos = primaryPhotos?.filter { it.isValid }?.map { it.createData() }
        val validOtherPhotos = otherPhotos?.filter { it.isValid }?.map { it.createData() }
        return if (metadata == null && validPrimaryPhotos == null && validOtherPhotos == null) {
            null
        } else {
            SearchResultMetadata(
                metadata = metadata ?: hashMapOf(),
                reviewCount = reviewCount,
                phone = phone,
                website = website,
                averageRating = averageRating,
                description = description,
                primaryPhotos = validPrimaryPhotos,
                otherPhotos = validOtherPhotos,
                openHours = openHours?.createData(),
                parking = parking?.createData(),
                cpsJson = cpsJson,
                rating = rating,
            )
        }
    }

    companion object {
        fun create(metadata: SearchResultMetadata?): SearchResultMetadataDAO? {
            metadata ?: return null
            return with(metadata) {
                SearchResultMetadataDAO(
                    metadata = coreMetadata.data,
                    reviewCount = reviewCount,
                    phone = phone,
                    website = website,
                    averageRating = averageRating,
                    description = description,
                    primaryPhotos = primaryPhotos?.map { ImageInfoDAO.create(it) },
                    otherPhotos = otherPhotos?.map { ImageInfoDAO.create(it) },
                    openHours = OpenHoursDAO.create(openHours),
                    parking = ParkingDataDAO.create(parking),
                    cpsJson = cpsJson,
                    rating = rating,
                )
            }
        }
    }
}
