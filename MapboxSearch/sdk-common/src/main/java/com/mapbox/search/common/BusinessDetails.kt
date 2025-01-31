package com.mapbox.search.common

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.metadata.ImageInfo
import kotlinx.parcelize.Parcelize

/**
 * This class contains information about a business, for example, EV Charging Station operator
 * [OCPI BusinessDetail](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#142-businessdetails-class).
 *
 * @property name Name of the business.
 * @property website Link to the website.
 * @property logo Image object representing link to the logo.
 */
@MapboxExperimental
@Parcelize
public class BusinessDetails @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val name: String,
    public val website: String? = null,
    public val logo: ImageInfo? = null
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BusinessDetails

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
        return "BusinessDetails(" +
                "name=$name, " +
                "website=$website, " +
                "logo=$logo" +
                ")"
    }
}
