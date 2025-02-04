package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

/**
 * Options for the offline search along a route.
 * @see OfflineSearchEngine
 *
 * @throws [IllegalArgumentException] if number of [route] points is less than 2
 */
@MapboxExperimental
@Parcelize
public class OfflineSearchAlongRouteOptions @JvmOverloads public constructor(

    /**
     * List of points that make up route line
     */
    public val route: List<Point>,

    /**
     * Bias the response to favor results that are closer to this location, provided as Point.
     */
    public val proximity: Point? = null,

    /**
     * Search origin point, used to calculate the distance to the search result.
     * @see [OfflineSearchResult.distanceMeters]
     */
    public val origin: Point? = null,

    /**
     * Specify the maximum number of results to return.
     */
    public val limit: Int? = null,

    /**
     * Optional offline EV options.
     */
    public val evSearchOptions: OfflineEvSearchOptions? = null,
) : Parcelable {

    init {
        require(route.size >= 2)
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineSearchAlongRouteOptions

        if (route != other.route) return false
        if (proximity != other.proximity) return false
        if (limit != other.limit) return false
        if (origin != other.origin) return false
        if (evSearchOptions != other.evSearchOptions) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = route.hashCode()
        result = 31 * result + (proximity?.hashCode() ?: 0)
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (origin?.hashCode() ?: 0)
        result = 31 * result + (evSearchOptions?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineSearchAlongRouteOptions(" +
                "route=$route, " +
                "proximity=$proximity, " +
                "limit=$limit, " +
                "origin=$origin, " +
                "evSearchOptions=$evSearchOptions" +
                ")"
    }
}
