package com.mapbox.search.common

import android.os.Parcelable
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

/**
 * Represents entry to the building, associated with original search result.
 */
@Parcelize
public class RoutablePoint(

    /**
     * Entry coordinates.
     */
    public val point: Point,

    /**
     * Entry name.
     */
    public val name: String
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoutablePoint

        if (point != other.point) return false
        if (name != other.name) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = point.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "RoutablePoint(" +
                "point=$point, " +
                "name='$name'" +
                ")"
    }
}
