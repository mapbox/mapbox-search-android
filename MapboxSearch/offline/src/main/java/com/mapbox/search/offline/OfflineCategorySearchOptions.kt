@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.RestrictedMapboxSearchAPI
import kotlinx.parcelize.Parcelize

/**
 * Options for the offline category search.
 * @see OfflineSearchEngine
 */
@MapboxExperimental
@Parcelize
public class OfflineCategorySearchOptions @JvmOverloads public constructor(

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
     * Optional offline EV options.
     *
     * Only specific datasets support EV filters and EV metadata.
     * Contact our [sales](https://www.mapbox.com/contact/sales) team to access datasets
     * that include EV charging station data.
     */
    @RestrictedMapboxSearchAPI
    public val evSearchOptions: OfflineEvSearchOptions? = null,

    /**
     * When set to true and multiple categories are requested, e.g.
     * `SearchEngine.search(listOf("coffee_shop", "hotel"), ...)`,
     * results will include at least one POI for each category, provided a POI is available
     * in a nearby location.
     *
     * A comma-separated list of multiple category values in the request determines the sort order
     * of the POI result. For example, for request
     * `SearchEngine.search(listOf("coffee_shop", "hotel"), ...)`, coffee_shop POI will be listed
     * first in the results.
     *
     * If there is more than one POI for categories, the number of search results will include
     * multiple features for each category. For example, assuming that
     * restaurant, coffee, parking_lot categories are requested and limit parameter is 10,
     * the result will be ranked as follows:
     * - 1st to 4th: restaurant POIs
     * - 5th to 7th: coffee POIs
     * - 8th to 10th: parking_lot POI
     */
    public val ensureResultsPerCategory: Boolean? = null,
) : Parcelable {

    init {
        check(limit == null || limit > 0) { "Provided limit should be greater than 0 (was found: $limit)." }
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineCategorySearchOptions

        if (proximity != other.proximity) return false
        if (limit != other.limit) return false
        if (origin != other.origin) return false
        if (boundingBox != other.boundingBox) return false
        if (evSearchOptions != other.evSearchOptions) return false
        if (ensureResultsPerCategory != other.ensureResultsPerCategory) return false

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
        result = 31 * result + evSearchOptions.hashCode()
        result = 31 * result + ensureResultsPerCategory.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineCategorySearchOptions(" +
                "proximity=$proximity," +
                "limit=$limit, " +
                "origin=$origin, " +
                "boundingBox=$boundingBox, " +
                "evSearchOptions=$evSearchOptions, " +
                "ensureResultsPerCategory=$ensureResultsPerCategory" +
                ")"
    }
}

@JvmSynthetic
internal fun OfflineCategorySearchOptions.mapToCore(): CoreSearchOptions = createCoreSearchOptions(
    proximity = proximity,
    origin = origin,
    bbox = boundingBox?.mapToCore(),
    limit = limit,
    evSearchOptions = evSearchOptions?.mapToCore(),
    ensureResultsPerCategory = ensureResultsPerCategory,
)
