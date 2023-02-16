package com.mapbox.search.autocomplete

import android.os.Parcelable
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

/**
 * Place Autocomplete suggestion type.
 */
@Parcelize
public class PlaceAutocompleteSuggestion internal constructor(
    private val result: PlaceAutocompleteResult
) : Parcelable {

    /**
     * Place's name.
     */
    public val name: String
        get() = result.name

    /**
     * Formatted address.
     */
    public val formattedAddress: String?
        get() = result.address?.formattedAddress

    /**
     * Place geographic point.
     */
    public val coordinate: Point
        get() = result.coordinate

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for the place.
     */
    public val makiIcon: String?
        get() = result.makiIcon

    /**
     * Distance in meters from place's coordinate to user location (if available).
     */
    public val distanceMeters: Double?
        get() = result.distanceMeters

    /**
     * The type of result.
     */
    public val type: PlaceAutocompleteType
        get() = result.type

    /**
     * Poi categories. Always empty for non-POI suggestions.
     * @see type
     */
    public val categories: List<String>?
        get() = result.categories

    /**
     * Returns resolved [PlaceAutocompleteResult] object.
     * @return resolved [PlaceAutocompleteResult] object.
     */
    public fun result(): PlaceAutocompleteResult {
        return result
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAutocompleteSuggestion

        if (result != other.result) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return result.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "PlaceAutocompleteSuggestion(" +
                "name='$name', " +
                "formattedAddress=$formattedAddress, " +
                "coordinate=$coordinate, " +
                "makiIcon=$makiIcon, " +
                "distanceMeters=$distanceMeters, " +
                "type=$type, " +
                "categories=$categories" +
                ")"
    }
}
