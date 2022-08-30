package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreReverseGeoOptions
import kotlinx.parcelize.Parcelize

/**
 * Options for offline reverse geocoding.
 * @see OfflineSearchEngine
 */
@Parcelize
public class OfflineReverseGeoOptions public constructor(
    /**
     * Coordinate to resolve.
     */
    public val center: Point,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineReverseGeoOptions

        if (center != other.center) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return center.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineReverseGeoOptions(center=$center)"
    }
}

@JvmSynthetic
internal fun OfflineReverseGeoOptions.mapToCore(): CoreReverseGeoOptions = CoreReverseGeoOptions(
    center,
    null,
    null,
    null,
    null,
    null,
)
