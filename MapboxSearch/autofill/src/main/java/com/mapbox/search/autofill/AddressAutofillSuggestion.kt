package com.mapbox.search.autofill

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.result.SearchAddress
import kotlinx.parcelize.Parcelize

/**
 * Address autofill suggestion type.
 */
@Parcelize
public class AddressAutofillSuggestion internal constructor(

    /**
     * Textual representation of the address.
     */
    public val formattedAddress: String,

    /**
     * Address geographic point.
     */
    public val coordinate: Point,

    /**
     * @suppress
     */
    private val address: SearchAddress,
) : Parcelable {

    /**
     * Returns resolved [AddressAutofillResult] object.
     * @return resolved [AddressAutofillResult] object.
     */
    public fun result(): AddressAutofillResult {
        return AddressAutofillResult(this, address)
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressAutofillSuggestion

        if (formattedAddress != other.formattedAddress) return false
        if (coordinate != other.coordinate) return false
        if (address != other.address) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = formattedAddress.hashCode()
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AddressAutofillSuggestion(formattedAddress='$formattedAddress', coordinate=$coordinate)"
    }
}

/**
 * Resolved [AddressAutofillSuggestion].
 */
@Parcelize
public class AddressAutofillResult internal constructor(

    /**
     * [AddressAutofillSuggestion] from which this result has been resolved.
     */
    public val suggestion: AddressAutofillSuggestion,

    /**
     * Detailed address components like street, house number, etc.
     */
    public val address: SearchAddress,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressAutofillResult

        if (suggestion != other.suggestion) return false
        if (address != other.address) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = suggestion.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AddressAutofillResult(suggestion=$suggestion, address=$address)"
    }
}
