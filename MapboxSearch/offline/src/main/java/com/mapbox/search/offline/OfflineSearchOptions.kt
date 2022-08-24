package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreSearchOptions
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
) : Parcelable {

    init {
        check(limit == null || limit > 0) { "Provided limit should be greater than 0 (was found: $limit)." }
    }

    /**
     * Creates new [OfflineSearchOptions] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        proximity: Point? = this.proximity,
        limit: Int? = this.limit,
        origin: Point? = this.origin,
    ): OfflineSearchOptions {
        return OfflineSearchOptions(
            proximity = proximity,
            limit = limit,
            origin = origin,
        )
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

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = proximity?.hashCode() ?: 0
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (origin?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineSearchOptions(proximity=$proximity, limit=$limit, origin=$origin)"
    }

    /**
     * Builder for [OfflineSearchOptions] instance creation.
     */
    public class Builder() {

        private var proximity: Point? = null
        private var limit: Int? = null
        private var origin: Point? = null

        internal constructor(options: OfflineSearchOptions) : this() {
            proximity = options.proximity
            limit = options.limit
            origin = options.origin
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
         * Create [OfflineSearchOptions] instance from builder data.
         */
        public fun build(): OfflineSearchOptions = OfflineSearchOptions(
            proximity = proximity,
            limit = limit,
            origin = origin,
        )
    }
}

@JvmSynthetic
internal fun OfflineSearchOptions.mapToCore(): CoreSearchOptions = CoreSearchOptions(
    proximity,
    origin,
    null,
    null,
    null,
    null,
    null,
    null,
    limit,
    null,
    false,
    null,
    null,
    null,
    null,
    null,
    null,
)
