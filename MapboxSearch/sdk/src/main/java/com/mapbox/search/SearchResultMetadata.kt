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
     * Average rating, associated with the search result.
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
     * TODO
     */
    @IgnoredOnParcel
    public val children: List<ChildMetadata>? = coreMetadata.children?.map { it.mapToPlatform() }

    @IgnoredOnParcel
    public val wheelchairAccessible: Boolean? = coreMetadata.wheelchairAccessible

    @IgnoredOnParcel
    public val delivery: Boolean? = coreMetadata.delivery

    @IgnoredOnParcel
    public val driveThrough: Boolean? = coreMetadata.driveThrough

    @IgnoredOnParcel
    public val reservable: Boolean? = coreMetadata.reservable

    @IgnoredOnParcel
    public val parkingAvailable: Boolean? = coreMetadata.parkingAvailable

    @IgnoredOnParcel
    public val valetParking: Boolean? = coreMetadata.parkingAvailable

    @IgnoredOnParcel
    public val streetParking: Boolean? = coreMetadata.streetParking

    @IgnoredOnParcel
    public val servesBreakfast: Boolean? = coreMetadata.servesBreakfast

    @IgnoredOnParcel
    public val servesBrunch: Boolean? = coreMetadata.servesBrunch

    @IgnoredOnParcel
    public val servesDinner: Boolean? = coreMetadata.servesDinner

    @IgnoredOnParcel
    public val servesLunch: Boolean? = coreMetadata.servesLunch

    @IgnoredOnParcel
    public val servesWine: Boolean? = coreMetadata.servesWine

    @IgnoredOnParcel
    public val servesBeer: Boolean? = coreMetadata.servesBeer

    @IgnoredOnParcel
    public val takeout: Boolean? = coreMetadata.takeout

    @IgnoredOnParcel
    public val facebookId: String? = coreMetadata.facebookId

    @IgnoredOnParcel
    public val fax: String? = coreMetadata.fax

    @IgnoredOnParcel
    public val email: String? = coreMetadata.email

    @IgnoredOnParcel
    public val instagram: String? = coreMetadata.instagram

    @IgnoredOnParcel
    public val twitter: String? = coreMetadata.twitter

    @IgnoredOnParcel
    public val priceLevel: String? = coreMetadata.priceLevel

    @IgnoredOnParcel
    public val servesVegan: Boolean? = coreMetadata.servesVegan

    @IgnoredOnParcel
    public val servesVegetarian: Boolean? = coreMetadata.servesVegetarian

    @IgnoredOnParcel
    public val rating: Float? = coreMetadata.rating

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
