package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

/**
 * OCPI AdditionalGeoLocation. This class defines an additional geographic location
 * that is relevant for the Charge Point. The geodetic system to be used is WGS 84.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#141-additionalgeolocation-class)
 * for more details.
 *
 * @property position Geographic position of the point.
 * @property name [EvDisplayText] object representing name of the point in local language or
 * as written at the location. For example the street name of a parking lot entrance
 * or its number.
 */
@MapboxExperimental
@Parcelize
public class EvAdditionalGeoLocation @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val position: Point,
    public val name: EvDisplayText? = null,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvAdditionalGeoLocation

        if (position != other.position) return false
        if (name != other.name) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AdditionalCpoGeoLocation(" +
                "position=$position, " +
                "name=$name" +
                ")"
    }
}
