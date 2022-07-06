package com.mapbox.search.autofill

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
    public val address: AddressComponents,
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
