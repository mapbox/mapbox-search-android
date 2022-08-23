package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.metadata.OpenHours
import com.mapbox.search.metadata.ParkingData
import com.mapbox.search.metadata.mapToCore
import com.mapbox.search.metadata.mapToPlatform
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Search result metadata container. Provides geo place's detailed information if available.
 * @see com.mapbox.search.result.SearchResult
 * @see com.mapbox.search.result.SearchSuggestion
 */
@Parcelize
public class SearchResultMetadata internal constructor(
    @get:JvmSynthetic
    internal val coreMetadata: CoreResultMetadata
) : Parcelable {

    /**
     * Raw extra data, e.g. the data that is not available via the remaining properties of this class.
     */
    @IgnoredOnParcel
    public val extraData: Map<String, String> = coreMetadata.data

    /**
     * Review count for associated search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val reviewCount: Int? = coreMetadata.reviewCount

    /**
     * Phone number, associated with the search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val phone: String? = coreMetadata.phone

    /**
     * Website, associated with the search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val website: String? = coreMetadata.website

    /**
     * Average rating, associated with the search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val averageRating: Double? = coreMetadata.avRating

    /**
     * Description of given search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val description: String? = coreMetadata.description

    /**
     * List of search result's primary photos.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val primaryPhotos: List<ImageInfo>? = coreMetadata.primaryPhoto?.map { it.mapToPlatform() }

    /**
     * List of search result's other (non-primary) photos.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val otherPhotos: List<ImageInfo>? = coreMetadata.otherPhoto?.map { it.mapToPlatform() }

    /**
     * Information about time periods, when given search result is opened or closed.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val openHours: OpenHours? = coreMetadata.openHours?.mapToPlatform()

    /**
     * Parking information for given search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val parking: ParkingData? = coreMetadata.parking?.mapToPlatform()

    /**
     * Raw CPS Specific Metadata for given search result, represented as a JSON string.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val cpsJson: String? = coreMetadata.cpsJson

    /**
     * The country code in ISO 3166-1.
     * Available for both [com.mapbox.search.result.SearchSuggestion] and [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val countryIso1: String? = coreMetadata.data["iso_3166_1"]

    /**
     * The country code and its country subdivision code in ISO 3166-2.
     * Available for both [com.mapbox.search.result.SearchSuggestion] and [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val countryIso2: String? = coreMetadata.data["iso_3166_2"]

    internal constructor(
        metadata: HashMap<String, String> = HashMap(),
        reviewCount: Int? = null,
        phone: String? = null,
        website: String? = null,
        averageRating: Double? = null,
        description: String? = null,
        primaryPhotos: List<ImageInfo>? = null,
        otherPhotos: List<ImageInfo>? = null,
        openHours: OpenHours? = null,
        parking: ParkingData? = null,
        cpsJson: String? = null,
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
                "extraData=$extraData, " +
                "reviewCount=$reviewCount, " +
                "phone=$phone, " +
                "website=$website, " +
                "averageRating=$averageRating, " +
                "description=$description, " +
                "primaryPhotos=$primaryPhotos, " +
                "otherPhotos=$otherPhotos, " +
                "openHours=$openHours, " +
                "parking=$parking, " +
                "cpsJson=$cpsJson, " +
                "countryIso1=$countryIso1, " +
                "countryIso2=$countryIso2" +
                ")"
    }
}
