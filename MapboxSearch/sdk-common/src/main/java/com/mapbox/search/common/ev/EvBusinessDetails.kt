package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * OCPI BusinessDetails. This class contains information about a business operator,
 * including its name, an optional website URL, and an optional logo image link. Refer to this type
 * in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#142-businessdetails-class)
 * for more details.
 *
 * @property name Name of the operator.
 * @property website Link to the operator’s website.
 * @property logo Image object representing link to the operator’s logo.
 */
@MapboxExperimental
@Parcelize
public class EvBusinessDetails @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val name: String,
    public val website: String? = null,
    public val logo: EvImage? = null
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvBusinessDetails

        if (name != other.name) return false
        if (website != other.website) return false
        if (logo != other.logo) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (logo?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvBusinessDetails(" +
                "name=$name, " +
                "website=$website, " +
                "logo=$logo" +
                ")"
    }
}
