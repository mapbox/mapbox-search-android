package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.utils.extension.mapToCore
import kotlinx.parcelize.Parcelize

/**
 * Options for the offline search.
 * @see OfflineSearchEngine
 */
@Parcelize
public class OfflineSearchOptions @JvmOverloads public constructor(

    /**
     * Bias the response to favor results that are closer to this location, provided as Point.
     */
    public val proximity: Point? = null,

    /**
     * Specify the maximum number of results to return.
     */
    public val limit: Int? = null,

    /**
     * Search origin point, used to calculate the distance to the search result.
     * @see [OfflineSearchResult.distanceMeters]
     */
    public val origin: Point? = null,

    /**
     * Limit results to only those contained within the supplied bounding box.
     * The bounding box cannot cross the 180th meridian (longitude +/-180.0 deg.)
     * and North or South pole (latitude +/- 90.0 deg.).
     */
    public val boundingBox: BoundingBox? = null,

    /**
     * By default, when [boundingBox] is applied, all results outside the bounding box
     * are filtered out. If [searchPlacesOutsideBondingBox] is set to true, the search
     * for places will be global, meaning results outside the [boundingBox] will not be
     * filtered out, even if a [boundingBox] is applied. Default if false.
     */
    public val searchPlacesOutsideBondingBox: Boolean = false
) : Parcelable {

    init {
        check(limit == null || limit > 0) { "Provided limit should be greater than 0 (was found: $limit)." }
    }

    /**
     * Creates new [OfflineSearchOptions.Builder] from current [OfflineSearchOptions] instance.
     */
    public fun toBuilder(): Builder {
        return Builder(this)
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchOptions

        if (proximity != other.proximity) return false
        if (limit != other.limit) return false
        if (origin != other.origin) return false
        if (boundingBox != other.boundingBox) return false
        if (searchPlacesOutsideBondingBox != other.searchPlacesOutsideBondingBox) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = proximity?.hashCode() ?: 0
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (origin?.hashCode() ?: 0)
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + searchPlacesOutsideBondingBox.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineSearchOptions(" +
                "proximity=$proximity," +
                " limit=$limit, " +
                "origin=$origin, " +
                "boundingBox=$boundingBox, " +
                "searchPlacesOutsideBondingBox=$searchPlacesOutsideBondingBox" +
                ")"
    }

    /**
     * Builder for [OfflineSearchOptions] instance creation.
     */
    public class Builder() {

        private var proximity: Point? = null
        private var limit: Int? = null
        private var origin: Point? = null
        private var boundingBox: BoundingBox? = null
        private var searchPlacesOutsideBondingBox: Boolean = false

        internal constructor(options: OfflineSearchOptions) : this() {
            proximity = options.proximity
            limit = options.limit
            origin = options.origin
            boundingBox = options.boundingBox
            searchPlacesOutsideBondingBox = options.searchPlacesOutsideBondingBox
        }

        /**
         * Bias the response to favor results that are closer to this location, provided as Point.
         */
        public fun proximity(proximity: Point?): Builder = apply { this.proximity = proximity }

        /**
         * Specify the maximum number of results to return. The maximum supported is 10.
         */
        public fun limit(limit: Int): Builder = apply { this.limit = limit }

        /**
         * Search origin point, used to calculate the distance to the search result.
         */
        public fun origin(origin: Point): Builder = apply { this.origin = origin }

        /**
         * Limit results to only those contained within the supplied bounding box.
         */
        public fun boundingBox(boundingBox: BoundingBox): Builder = apply { this.boundingBox = boundingBox }

        /**
         * By default, when [boundingBox] is applied, all results outside the bounding box
         * are filtered out. If [searchPlacesOutsideBondingBox] is set to true, the search
         * for places will be global, meaning results outside the [boundingBox] will not be
         * filtered out, even if a [boundingBox] is applied. Default if false.
         */
        public fun searchPlacesOutsideBondingBox(
            searchPlacesOutsideBondingBox: Boolean,
        ): Builder = apply {
            this.searchPlacesOutsideBondingBox = searchPlacesOutsideBondingBox
        }

        /**
         * Create [OfflineSearchOptions] instance from builder data.
         */
        public fun build(): OfflineSearchOptions = OfflineSearchOptions(
            proximity = proximity,
            limit = limit,
            origin = origin,
            boundingBox = boundingBox,
            searchPlacesOutsideBondingBox = searchPlacesOutsideBondingBox,
        )
    }
}

@JvmSynthetic
internal fun OfflineSearchOptions.mapToCore(): CoreSearchOptions = createCoreSearchOptions(
    proximity = proximity,
    origin = origin,
    bbox = boundingBox?.mapToCore(),
    limit = limit,
    offlineSearchPlacesOutsideBbox = searchPlacesOutsideBondingBox,
)
