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
     * Result ID
     */
    public val id: String,

    /**
     * Result MapboxID
     */
    public val mapboxId: String?,

    /**
     * [AddressAutofillSuggestion] from which this result has been resolved.
     */
    public val suggestion: AddressAutofillSuggestion,

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
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressAutofillResult

        if (id != other.id) return false
        if (mapboxId != other.mapboxId) return false
        if (suggestion != other.suggestion) return false
        if (coordinate != other.coordinate) return false
        if (address != other.address) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + mapboxId.hashCode()
        result = 31 * result + suggestion.hashCode()
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AddressAutofillResult(id=$id, mapboxId=$mapboxId, suggestion=$suggestion, coordinate=$coordinate, address=$address)"
    }
}
