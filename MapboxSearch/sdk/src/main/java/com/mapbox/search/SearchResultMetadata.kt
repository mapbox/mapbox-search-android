package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.core.CoreResultMetadata
import com.mapbox.search.metadata.OpenHours
import com.mapbox.search.metadata.ParkingData
import com.mapbox.search.metadata.mapToCore
import com.mapbox.search.metadata.mapToPlatform
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Search result metadata container. Provides geo place's detailed information if available.
 * @see com.mapbox.search.result.SearchResult
 */
@Parcelize
public class SearchResultMetadata internal constructor(
    @get:JvmSynthetic
    internal val coreMetadata: CoreResultMetadata
) : Parcelable {

    /**
     * Review count for associated search result.
     */
    @IgnoredOnParcel
    public val reviewCount: Int? = coreMetadata.reviewCount

    /**
     * Phone number, associated with the search result.
     */
    @IgnoredOnParcel
    public val phone: String? = coreMetadata.phone

    /**
     * Website, associated with the search result.
     */
    @IgnoredOnParcel
    public val website: String? = coreMetadata.website

    /**
     * Average rating, associated with the search result.
     */
    @IgnoredOnParcel
    public val averageRating: Double? = coreMetadata.avRating

    /**
     * Description of given search result.
     */
    @IgnoredOnParcel
    public val description: String? = coreMetadata.description

    /**
     * List of search result's primary photos.
     */
    @IgnoredOnParcel
    public val primaryPhotos: List<ImageInfo>? = coreMetadata.primaryPhoto?.map { it.mapToPlatform() }

    /**
     * List of search result's other (non-primary) photos.
     */
    @IgnoredOnParcel
    public val otherPhotos: List<ImageInfo>? = coreMetadata.otherPhoto?.map { it.mapToPlatform() }

    /**
     * Raw extra data, e.g. the data that is not available via the remaining properties of this class.
     */
    @IgnoredOnParcel
    public val extraData: Map<String, String> = coreMetadata.data

    /**
     * Information about time periods, when given search result is opened or closed.
     */
    @IgnoredOnParcel
    public val openHours: OpenHours? = coreMetadata.openHours?.mapToPlatform()

    /**
     * Parking information for given search result.
     */
    @IgnoredOnParcel
    public val parking: ParkingData? = coreMetadata.parking?.mapToPlatform()

    /**
     * Raw CPS Specific Metadata for given search result, represented as a JSON string.
     */
    @IgnoredOnParcel
    public val cpsJson: String? = coreMetadata.cpsJson

    internal constructor(
        metadata: HashMap<String, String>,
        reviewCount: Int?,
        phone: String?,
        website: String?,
        averageRating: Double?,
        description: String?,
        primaryPhotos: List<ImageInfo>?,
        otherPhotos: List<ImageInfo>?,
        openHours: OpenHours?,
        parking: ParkingData?,
        cpsJson: String?
    ) : this(
        CoreResultMetadata(
            reviewCount,
            phone,
            website,
            averageRating,
            description,
            openHours?.mapToCore(),
            primaryPhotos?.map { it.mapToCore() },
            otherPhotos?.map { it.mapToCore() },
            cpsJson,
            parking?.mapToCore(),
            metadata
        )
    )

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResultMetadata

        if (coreMetadata != other.coreMetadata) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return coreMetadata.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchResultMetadata(" +
                "reviewCount=$reviewCount, phone=$phone, website=$website, averageRating=$averageRating, " +
                "description=$description, primaryPhotos=$primaryPhotos, otherPhotos=$otherPhotos, extraData=$extraData, " +
                "openHours=$openHours, parking=$parking, cpsJson=$cpsJson" +
                ")"
    }
}
