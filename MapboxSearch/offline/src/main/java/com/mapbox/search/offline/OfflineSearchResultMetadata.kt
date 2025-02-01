package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.factory.mapToPlatform
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.common.Facility
import com.mapbox.search.common.LocalizedText
import com.mapbox.search.common.ParkingType
import com.mapbox.search.common.ev.EvMetadata
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Metadata container for the offline search results.
 */
@MapboxExperimental
@Parcelize
public class OfflineSearchResultMetadata internal constructor(
    @get:JvmSynthetic
    internal val coreMetadata: CoreResultMetadata
) : Parcelable {

    /**
     * Raw extra data, e.g. the data that is not available via the remaining properties of this class.
     */
    @IgnoredOnParcel
    public val extraData: Map<String, String> = coreMetadata.data

    /**
     * Website, associated with the search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val website: String? = coreMetadata.website

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
     * Indicates whether parking is available at the location.
     */
    @IgnoredOnParcel
    public val parkingAvailable: Boolean? = coreMetadata.parkingAvailable

    /**
     * The type of parking at the POI.
     */
    @IgnoredOnParcel
    @ParkingType.Type
    public val parkingType: String? = coreMetadata.parkingType?.mapToPlatform()

    /**
     * Human-readable directions on how to reach the location.
     */
    @IgnoredOnParcel
    public val directions: List<LocalizedText>? = coreMetadata.directions?.map { it.mapToPlatform() }

    /**
     * Indicates the availability of street parking near the location.
     */
    @IgnoredOnParcel
    public val streetParking: Boolean? = coreMetadata.streetParking

    /**
     * Provides EV-related metadata
     */
    @IgnoredOnParcel
    public val evMetadata: EvMetadata? = coreMetadata.evMetadata?.mapToPlatform()

    /**
     * List of [Facility.Type] values this POI directly belongs to.
     */
    @IgnoredOnParcel
    public val facilities: List<String>? = coreMetadata.facilities?.map { it.mapToPlatform() }

    /**
     * One of IANA time zone data’s TZ-values representing the time zone of the location.
     */
    @IgnoredOnParcel
    public val timezone: String? = coreMetadata.timezone

    /**
     * Timestamp in RFC 3339 format when POI data were last updated (or created).
     */
    @IgnoredOnParcel
    public val lastUpdated: String? = coreMetadata.lastUpdated

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchResultMetadata

        return coreMetadata == other.coreMetadata
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
        return "OfflineSearchResultMetadata(" +
                "extraData=$extraData, " +
                "website=$website, " +
                "primaryPhotos=$primaryPhotos, " +
                "otherPhotos=$otherPhotos, " +
                "openHours=$openHours, " +
                "parkingAvailable=$parkingAvailable, " +
                "parkingType=$parkingType, " +
                "directions=$directions, " +
                "streetParking=$streetParking, " +
                "evMetadata=$evMetadata, " +
                "facilities=$facilities, " +
                "timezone=$timezone, " +
                "lastUpdated=$lastUpdated" +
                ")"
    }
}
