package com.mapbox.search.autocomplete

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.base.utils.extension.nullIfEmpty
import com.mapbox.search.common.RoutablePoint
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
     * Place's address.
     */
    public val address: PlaceAutocompleteAddress?,

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
        if (distanceMeters != other.distanceMeters) return false
        if (address != other.address) return false
        if (phone != other.phone) return false
        if (website != other.website) return false
        if (reviewCount != other.reviewCount) return false
        if (averageRating != other.averageRating) return false
        if (openHours != other.openHours) return false

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
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (phone?.hashCode() ?: 0)
        result = 31 * result + (website?.hashCode() ?: 0)
        result = 31 * result + (reviewCount ?: 0)
        result = 31 * result + (averageRating?.hashCode() ?: 0)
        result = 31 * result + (openHours?.hashCode() ?: 0)
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
                "address=$address, " +
                "phone=$phone, " +
                "website=$website, " +
                "reviewCount=$reviewCount, " +
                "averageRating=$averageRating, " +
                "openHours=$openHours" +
                ")"
    }

    internal companion object {

        @JvmSynthetic
        fun createFromSearchResult(result: BaseSearchResult): PlaceAutocompleteResult {
            with(result) {
                val discoverAddress = PlaceAutocompleteAddress(
                    houseNumber = address?.houseNumber?.nullIfEmpty(),
                    street = address?.street?.nullIfEmpty(),
                    neighborhood = address?.neighborhood?.nullIfEmpty(),
                    locality = address?.locality?.nullIfEmpty(),
                    postcode = address?.postcode?.nullIfEmpty(),
                    place = address?.place?.nullIfEmpty(),
                    district = address?.district?.nullIfEmpty(),
                    region = address?.region?.nullIfEmpty(),
                    country = address?.country?.nullIfEmpty(),
                    formattedAddress = result.fullAddress ?: result.descriptionText,
                    countryIso1 = metadata?.countryIso1,
                    countryIso2 = metadata?.countryIso2
                )

                return PlaceAutocompleteResult(
                    name = name,
                    coordinate = coordinate,
                    routablePoints = routablePoints?.map { it.mapToPlatform() },
                    makiIcon = makiIcon,
                    distanceMeters = distanceMeters,
                    address = discoverAddress,
                    phone = metadata?.phone,
                    website = metadata?.website,
                    reviewCount = metadata?.reviewCount,
                    averageRating = metadata?.avRating,
                    openHours = metadata?.openHours?.mapToPlatform()
                )
            }
        }
    }
}
