package com.mapbox.search.autocomplete

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import kotlinx.parcelize.Parcelize

/**
 * Resolved [PlaceAutocompleteSuggestion].
 */
@Parcelize
public class PlaceAutocompleteResult internal constructor(

    /**
     * Place's name.
     */
    public val name: String,

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
     * Place's address.
     */
    public val address: PlaceAutocompleteAddress?,

    /**
     * The type of result.
     */
    public val type: PlaceAutocompleteType,

    /**
     * Poi categories. Always empty for non-POI results.
     * @see type
     */
    public val categories: List<String>?,

    /**
     * Business phone number.
     */
    public val phone: String?,

    /**
     * Business website.
     */
    public val website: String?,

    /**
     * Number of reviews.
     */
    public val reviewCount: Int?,

    /**
     * Average rating.
     */
    public val averageRating: Double?,

    /**
     * Business opening hours.
     */
    public val openHours: OpenHours?,

    /**
     * List of place's primary photos.
     */
    public val primaryPhotos: List<ImageInfo>?,

    /**
     * List of place's photos (non-primary).
     */
    public val otherPhotos: List<ImageInfo>?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAutocompleteResult

        if (name != other.name) return false
        if (coordinate != other.coordinate) return false
        if (routablePoints != other.routablePoints) return false
        if (makiIcon != other.makiIcon) return false
        if (!distanceMeters.safeCompareTo(other.distanceMeters)) return false
        if (!etaMinutes.safeCompareTo(other.etaMinutes)) return false
        if (address != other.address) return false
        if (type != other.type) return false
        if (categories != other.categories) return false
        if (phone != other.phone) return false
        if (website != other.website) return false
        if (reviewCount != other.reviewCount) return false
        if (!averageRating.safeCompareTo(other.averageRating)) return false
        if (openHours != other.openHours) return false
        if (primaryPhotos != other.primaryPhotos) return false
        if (otherPhotos != other.otherPhotos) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + (routablePoints?.hashCode() ?: 0)
        result = 31 * result + (makiIcon?.hashCode() ?: 0)
        result = 31 * result + (distanceMeters?.hashCode() ?: 0)
        result = 31 * result + (etaMinutes?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + (categories?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (reviewCount ?: 0)
        result = 31 * result + (averageRating?.hashCode() ?: 0)
        result = 31 * result + (openHours?.hashCode() ?: 0)
        result = 31 * result + (primaryPhotos?.hashCode() ?: 0)
        result = 31 * result + (otherPhotos?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "PlaceAutocompleteResult(" +
                "name='$name', " +
                "coordinate=$coordinate, " +
                "routablePoints=$routablePoints, " +
                "makiIcon=$makiIcon, " +
                "distanceMeters=$distanceMeters, " +
                "etaMinutes=$etaMinutes, " +
                "address=$address, " +
                "type=$type, " +
                "categories=$categories, " +
                "phone=$phone, " +
                "website=$website, " +
                "reviewCount=$reviewCount, " +
                "averageRating=$averageRating, " +
                "openHours=$openHours, " +
                "primaryPhotos=$primaryPhotos, " +
                "otherPhotos=$otherPhotos" +
                ")"
    }
}
