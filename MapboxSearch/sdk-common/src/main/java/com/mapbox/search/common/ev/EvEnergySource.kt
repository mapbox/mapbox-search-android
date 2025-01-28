package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * OCPI EnergySource. Key-value pairs (enum + percentage) of energy sources. All given values of all categories should add up to 100 percent.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#147-energysource-class)
 * for more details.
 *
 * @property source [EvEnergySourceCategory.Type] value indicating the type of energy source.
 * @property percentage Percentage of this source (0-100) in the mix.
 */
@MapboxExperimental
@Parcelize
public class EvEnergySource @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    @EvEnergySourceCategory.Type public val source: String,
    public val percentage: Int
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvEnergySource

        if (source != other.source) return false
        if (percentage != other.percentage) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + percentage
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvEnergySource(" +
                "source=$source, " +
                "percentage=$percentage" +
                ")"
    }
}
