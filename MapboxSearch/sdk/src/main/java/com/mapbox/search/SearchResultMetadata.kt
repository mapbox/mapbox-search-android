package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.mapToCore
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.common.metadata.ChildMetadata
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.ParkingData
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Search result metadata container. Provides geo place's detailed information if available.
 * @see com.mapbox.search.result.SearchResult
 * @see com.mapbox.search.result.SearchSuggestion
 */
@Parcelize
public class SearchResultMetadata internal constructor(
    @get:JvmSynthetic
    internal val coreMetadata: CoreResultMetadata
) : Parcelable {

    /**
     * Raw extra data, e.g. the data that is not available via the remaining properties of this class.
     */
    @IgnoredOnParcel
    public val extraData: Map<String, String> = coreMetadata.data

    /**
     * Review count for associated search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val reviewCount: Int? = coreMetadata.reviewCount

    /**
     * Phone number, associated with the search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val phone: String? = coreMetadata.phone

    /**
     * Website, associated with the search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val website: String? = coreMetadata.website

    /**
     * [Deprecated] Average rating, associated with the search result. Use `rating` instead. This will be removed in v3.0.0
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val averageRating: Double? = coreMetadata.avRating

    /**
     * Description of given search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val description: String? = coreMetadata.description

    /**
     * List of search result's primary photos.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val primaryPhotos: List<ImageInfo>? = coreMetadata.primaryPhoto?.map { it.mapToPlatform() }

    /**
     * List of search result's other (non-primary) photos.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val otherPhotos: List<ImageInfo>? = coreMetadata.otherPhoto?.map { it.mapToPlatform() }

    /**
     * Information about time periods, when given search result is opened or closed.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val openHours: OpenHours? = coreMetadata.openHours?.mapToPlatform()

    /**
     * Parking information for given search result.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val parking: ParkingData? = coreMetadata.parking?.mapToPlatform()

    /**
     * Raw CPS Specific Metadata for given search result, represented as a JSON string.
     * Available only for resolved [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val cpsJson: String? = coreMetadata.cpsJson

    /**
     * The country code in ISO 3166-1.
     * Available for both [com.mapbox.search.result.SearchSuggestion] and [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val countryIso1: String? = coreMetadata.data["iso_3166_1"]

    /**
     * The country code and its country subdivision code in ISO 3166-2.
     * Available for both [com.mapbox.search.result.SearchSuggestion] and [com.mapbox.search.result.SearchResult].
     */
    @IgnoredOnParcel
    public val countryIso2: String? = coreMetadata.data["iso_3166_2"]

    /**
     * Child metadata for POI.
     */
    @IgnoredOnParcel
    public val children: List<ChildMetadata>? = coreMetadata.children?.map { it.mapToPlatform() }

    /**
     * Indicates whether the location is accessible by wheelchair.
     */
    @IgnoredOnParcel
    public val wheelchairAccessible: Boolean? = coreMetadata.wheelchairAccessible

    /**
     * Indicates whether the location offers delivery services
     */
    @IgnoredOnParcel
    public val delivery: Boolean? = coreMetadata.delivery

    /**
     * Indicates whether the location has a drive-through service.
     */
    @IgnoredOnParcel
    public val driveThrough: Boolean? = coreMetadata.driveThrough

    /**
     * Indicates whether the location accepts reservations.
     */
    @IgnoredOnParcel
    public val reservable: Boolean? = coreMetadata.reservable

    /**
     * Indicates whether parking is available at the location.
     */
    @IgnoredOnParcel
    public val parkingAvailable: Boolean? = coreMetadata.parkingAvailable

    /**
     * Indicates whether valet parking services are offered.
     */
    @IgnoredOnParcel
    public val valetParking: Boolean? = coreMetadata.valetParking

    /**
     * Indicates the availability of street parking near the location.
     */
    @IgnoredOnParcel
    public val streetParking: Boolean? = coreMetadata.streetParking

    /**
     * Indicates whether breakfast is served.
     */
    @IgnoredOnParcel
    public val servesBreakfast: Boolean? = coreMetadata.servesBreakfast

    /**
     * Indicates whether brunch is served.
     */
    @IgnoredOnParcel
    public val servesBrunch: Boolean? = coreMetadata.servesBrunch

    /**
     * Indicates whether dinner is served.
     */
    @IgnoredOnParcel
    public val servesDinner: Boolean? = coreMetadata.servesDinner

    /**
     * Indicates whether lunch is served.
     */
    @IgnoredOnParcel
    public val servesLunch: Boolean? = coreMetadata.servesLunch

    /**
     * Indicates whether wine is served.
     */
    @IgnoredOnParcel
    public val servesWine: Boolean? = coreMetadata.servesWine

    /**
     * Indicates whether beer is served.
     */
    @IgnoredOnParcel
    public val servesBeer: Boolean? = coreMetadata.servesBeer

    /**
     * Indicates whether takeout services are available.
     */
    @IgnoredOnParcel
    public val takeout: Boolean? = coreMetadata.takeout

    /**
     * The Facebook ID associated with the feature.
     */
    @IgnoredOnParcel
    public val facebookId: String? = coreMetadata.facebookId

    /**
     * The fax number associated with the location.
     */
    @IgnoredOnParcel
    public val fax: String? = coreMetadata.fax

    /**
     * The email address associated with the location.
     */
    @IgnoredOnParcel
    public val email: String? = coreMetadata.email

    /**
     * The Instagram handle associated with the location.
     */
    @IgnoredOnParcel
    public val instagram: String? = coreMetadata.instagram

    /**
     * The Twitter handle associated with the location.
     */
    @IgnoredOnParcel
    public val twitter: String? = coreMetadata.twitter

    /**
     * The price level of the location, represented by a string including dollar signs.
     * The values scale from Cheap "$" to Most Expensive "$$$$".
     */
    @IgnoredOnParcel
    public val priceLevel: String? = coreMetadata.priceLevel

    /**
     * Indicates whether vegan diet options are available.
     */
    @IgnoredOnParcel
    public val servesVegan: Boolean? = coreMetadata.servesVegan

    /**
     * Indicates whether vegetarian diet options are available.
     */
    @IgnoredOnParcel
    public val servesVegetarian: Boolean? = coreMetadata.servesVegetarian

    /**
     * The average rating of the location, on a scale from 1 to 5.
     */
    @IgnoredOnParcel
    public val rating: Float? = coreMetadata.rating

    /**
     * A popularity score for the location, calculated based on user engagement and review counts.
     * The value scales from 0 to 1, 1 being the most popular.
     */
    @IgnoredOnParcel
    public val popularity: Float? = coreMetadata.popularity

    internal constructor(
        metadata: HashMap<String, String> = HashMap(),
        reviewCount: Int? = null,
        phone: String? = null,
        website: String? = null,
        averageRating: Double? = null,
        description: String? = null,
        primaryPhotos: List<ImageInfo>? = null,
        otherPhotos: List<ImageInfo>? = null,
        openHours: OpenHours? = null,
        parking: ParkingData? = null,
        children: List<ChildMetadata>? = null,
        cpsJson: String? = null,
        wheelchairAccessible: Boolean? = null,
        delivery: Boolean? = null,
        driveThrough: Boolean? = null,
        reservable: Boolean? = null,
        parkingAvailable: Boolean? = null,
        valetParking: Boolean? = null,
        streetParking: Boolean? = null,
        servesBreakfast: Boolean? = null,
        servesBrunch: Boolean? = null,
        servesDinner: Boolean? = null,
        servesLunch: Boolean? = null,
        servesWine: Boolean? = null,
        servesBeer: Boolean? = null,
        takeout: Boolean? = null,
        facebookId: String? = null,
        fax: String? = null,
        email: String? = null,
        instagram: String? = null,
        twitter: String? = null,
        priceLevel: String? = null,
        servesVegan: Boolean? = null,
        servesVegetarian: Boolean? = null,
        rating: Float? = null,
        popularity: Float? = null,
    ) : this(
        CoreResultMetadata(
            reviewCount,
            phone,
            website,
            averageRating,
            description,
            openHours?.mapToCore(),
            primaryPhotos?.map { it.mapToCore() },
            otherPhotos?.map { it.mapToCore() },
            cpsJson,
            parking?.mapToCore(),
            children?.map { it.mapToCore() },
            metadata,
            wheelchairAccessible,
            delivery,
            driveThrough,
            reservable,
            parkingAvailable,
            valetParking,
            streetParking,
            servesBreakfast,
            servesBrunch,
            servesDinner,
            servesLunch,
            servesWine,
            servesBeer,
            takeout,
            facebookId,
            fax,
            email,
            instagram,
            twitter,
            priceLevel,
            servesVegan,
            servesVegetarian,
            rating,
            popularity,
        )
    )

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResultMetadata

        if (coreMetadata != other.coreMetadata) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return coreMetadata.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchResultMetadata(" +
                "extraData=$extraData, " +
                "reviewCount=$reviewCount, " +
                "phone=$phone, " +
                "website=$website, " +
                "averageRating=$averageRating, " +
                "description=$description, " +
                "primaryPhotos=$primaryPhotos, " +
                "otherPhotos=$otherPhotos, " +
                "openHours=$openHours, " +
                "parking=$parking, " +
                "cpsJson=$cpsJson, " +
                "countryIso1=$countryIso1, " +
                "countryIso2=$countryIso2, " +
                "children=$children, " +
                "wheelchairAccessible=$wheelchairAccessible, " +
                "delivery=$delivery, " +
                "driveThrough=$driveThrough, " +
                "reservable=$reservable, " +
                "parkingAvailable=$parkingAvailable, " +
                "valetParking=$valetParking, " +
                "streetParking=$streetParking, " +
                "servesBreakfast=$servesBreakfast, " +
                "servesBrunch=$servesBrunch, " +
                "servesDinner=$servesDinner, " +
                "servesLunch=$servesLunch, " +
                "servesWine=$servesWine, " +
                "servesBeer=$servesBeer, " +
                "takeout=$takeout, " +
                "facebookId=$facebookId, " +
                "fax=$fax, " +
                "email=$email, " +
                "instagram=$instagram, " +
                "twitter=$twitter, " +
                "priceLevel=$priceLevel, " +
                "servesVegan=$servesVegan, " +
                "servesVegetarian=$servesVegetarian, " +
                "rating=$rating, " +
                "popularity=$popularity" +
                ")"
    }
}
