package com.mapbox.search.autofill

import android.os.Parcelable
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

/**
 * Resolved [AddressAutofillSuggestion].
 */
@Parcelize
public class AddressAutofillResult internal constructor(

    /**
     * Place geographic point.
     */
    public val coordinate: Point,

    /**
     * Detailed address components like street, house number, etc.
     */
    public val address: AddressComponents,
) : Parcelable {

    /**
     * Formatted address string
     */
    public val formattedAddress: String
        get() = address.formattedAddress()

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressAutofillResult

        if (coordinate != other.coordinate) return false
        if (address != other.address) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = coordinate.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AddressAutofillResult(coordinate=$coordinate, address=$address)"
    }
}
