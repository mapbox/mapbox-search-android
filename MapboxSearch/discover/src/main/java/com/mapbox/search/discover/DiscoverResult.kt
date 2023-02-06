package com.mapbox.search.discover

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.base.utils.extension.nullIfEmpty
import com.mapbox.search.common.RoutablePoint
import kotlinx.parcelize.Parcelize

/**
 * Information about a place returned by the [Discover].
 */
@Parcelize
public class DiscoverResult internal constructor(

    /**
     * Place's name.
     */
    public val name: String,

    /**
     * Place's address.
     */
    public val address: DiscoverAddress,

    /**
     * Address geographic point.
     */
    public val coordinate: Point,

    /**
     * List of points near [coordinate], that represents entries to associated building.
     */
    public val routablePoints: List<RoutablePoint>?,

    /**
     * Poi categories.
     */
    public val categories: List<String>,

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for the place.
     */
    public val makiIcon: String?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiscoverResult

        if (name != other.name) return false
        if (address != other.address) return false
        if (coordinate != other.coordinate) return false
        if (routablePoints != other.routablePoints) return false
        if (categories != other.categories) return false
        if (makiIcon != other.makiIcon) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + (routablePoints?.hashCode() ?: 0)
        result = 31 * result + categories.hashCode()
        result = 31 * result + (makiIcon?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "DiscoverApiResult(" +
                "name='$name', " +
                "address=$address, " +
                "coordinate=$coordinate, " +
                "routablePoints=$routablePoints, " +
                "categories=$categories, " +
                "makiIcon=$makiIcon" +
                ")"
    }

    internal companion object {

        @JvmSynthetic
        fun createFromSearchResult(result: BaseSearchResult): DiscoverResult {
            with(result) {
                val discoverAddress = DiscoverAddress(
                    houseNumber = address?.houseNumber?.nullIfEmpty(),
                    street = address?.street?.nullIfEmpty(),
                    neighborhood = address?.neighborhood?.nullIfEmpty(),
                    locality = address?.locality?.nullIfEmpty(),
                    postcode = address?.postcode?.nullIfEmpty(),
                    place = address?.place?.nullIfEmpty(),
                    district = address?.district?.nullIfEmpty(),
                    region = address?.region?.nullIfEmpty(),
                    country = address?.country?.nullIfEmpty(),
                    countryIso1 = metadata?.countryIso1,
                    countryIso2 = metadata?.countryIso2
                )

                return DiscoverResult(
                    name = name,
                    address = discoverAddress,
                    coordinate = coordinate,
                    routablePoints = routablePoints?.map { it.mapToPlatform() },
                    categories = categories ?: emptyList(),
                    makiIcon = makiIcon
                )
            }
        }
    }
}
