package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.factory.ev.toPlatform
import com.mapbox.search.base.mapToPlatform
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
     * Indicates the availability of street parking near the location.
     */
    @IgnoredOnParcel
    public val streetParking: Boolean? = coreMetadata.streetParking

    /**
     * Provides EV-related metadata
     */
    @IgnoredOnParcel
    public val evMetadata: EvMetadata? = coreMetadata.evMetadata?.toPlatform()

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
                "streetParking=$streetParking, " +
                "evMetadata=$evMetadata" +
                ")"
    }
}
