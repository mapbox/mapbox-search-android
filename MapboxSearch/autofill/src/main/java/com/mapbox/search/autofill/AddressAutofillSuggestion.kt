package com.mapbox.search.autofill

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.result.BaseSearchSuggestion
import kotlinx.parcelize.Parcelize

/**
 * Address autofill suggestion type.
 */
@Parcelize
public class AddressAutofillSuggestion internal constructor(

    /**
     * Suggestion name.
     */
    public val name: String,

    /**
     * Textual representation of the address.
     */
    public val formattedAddress: String,

    /**
     * Address geographic point.
     */
    public val coordinate: Point?,

    /**
     * @suppress
     */
    @JvmSynthetic
    internal val address: AddressComponents,

    @JvmSynthetic
    internal val underlying: BaseSearchSuggestion?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressAutofillSuggestion

        if (name != other.name) return false
        if (formattedAddress != other.formattedAddress) return false
        if (coordinate != other.coordinate) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + formattedAddress.hashCode()
        result = 31 * result + (coordinate?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AddressAutofillSuggestion(" +
                "name='$name', " +
                "formattedAddress='$formattedAddress', " +
                "coordinate=$coordinate" +
                ")"
    }
}
