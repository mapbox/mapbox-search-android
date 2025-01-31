package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * OCPI EnvironmentalImpact.
 * Amount of waste produced/emitted per kWh.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#149-environmentalimpact-class)
 * for more details.
 *
 * @property category [EvEnvironmentalImpactCategory] value indicating the environmental impact category.
 * @property amount Amount of this part in g/kWh.
 */
@MapboxExperimental
@Parcelize
public class EvEnvironmentalImpact @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    @EvEnvironmentalImpactCategory.Type public val category: String,
    public val amount: Float?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvEnvironmentalImpact

        if (category != other.category) return false
        if (amount != other.amount) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = category.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvEnvironmentalImpact(category=$category, amount=$amount)"
    }
}
