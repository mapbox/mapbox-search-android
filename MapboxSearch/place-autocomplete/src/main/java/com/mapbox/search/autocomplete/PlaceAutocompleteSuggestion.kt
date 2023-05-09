package com.mapbox.search.autocomplete

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.RoutablePoint
import kotlinx.parcelize.Parcelize

/**
 * Place Autocomplete suggestion type.
 */
@Parcelize
public class PlaceAutocompleteSuggestion internal constructor(

    /**
     * Place's name.
     */
    public val name: String,

    /**
     * Formatted address.
     */
    public val formattedAddress: String?,

    /**
     * Place geographic point.
     */
    public val coordinate: Point,

    /**
     * List of points near [coordinate], that represents entries to associated building.
     */
    public val routablePoints: List<RoutablePoint>?,

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for the place.
     */
    public val makiIcon: String?,

    /**
     * Distance in meters from place's coordinate to user location (if available).
     */
    public val distanceMeters: Double?,

    /**
     * Estimated time of arrival (in minutes) based on the specified navigation profile.
     */
    public val etaMinutes: Double?,

    /**
     * The type of result.
     */
    public val type: PlaceAutocompleteType,

    /**
     * Poi categories. Always empty for non-POI suggestions.
     * @see type
     */
    public val categories: List<String>?,

    /**
     * Underlying data on which this [PlaceAutocompleteSuggestion] is based.
     */
    @JvmSynthetic
    internal val underlying: Underlying
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAutocompleteSuggestion

        if (name != other.name) return false
        if (formattedAddress != other.formattedAddress) return false
        if (coordinate != other.coordinate) return false
        if (routablePoints != other.routablePoints) return false
        if (makiIcon != other.makiIcon) return false
        if (!distanceMeters.safeCompareTo(other.distanceMeters)) return false
        if (!etaMinutes.safeCompareTo(other.etaMinutes)) return false
        if (type != other.type) return false
        if (categories != other.categories) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (formattedAddress?.hashCode() ?: 0)
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + (routablePoints?.hashCode() ?: 0)
        result = 31 * result + (makiIcon?.hashCode() ?: 0)
        result = 31 * result + (distanceMeters?.hashCode() ?: 0)
        result = 31 * result + (etaMinutes?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + (categories?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "PlaceAutocompleteSuggestion(" +
                "name='$name', " +
                "formattedAddress=$formattedAddress, " +
                "coordinate=$coordinate, " +
                "routablePoints=$routablePoints, " +
                "makiIcon=$makiIcon, " +
                "distanceMeters=$distanceMeters, " +
                "etaMinutes=$etaMinutes, " +
                "type=$type, " +
                "categories=$categories" +
                ")"
    }

    internal sealed class Underlying : Parcelable {

        @Parcelize
        data class Suggestion(val suggestion: BaseSearchSuggestion) : Underlying()

        @Parcelize
        data class Result(val result: BaseSearchResult) : Underlying()
    }
}
