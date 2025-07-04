@file:OptIn(MapboxExperimental::class, RestrictedMapboxSearchAPI::class)

package com.mapbox.search

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.createCoreResultMetadata
import com.mapbox.search.base.factory.mapToCore
import com.mapbox.search.base.factory.mapToPlatform
import com.mapbox.search.base.factory.parking.mapToCore
import com.mapbox.search.base.factory.parking.mapToPlatform
import com.mapbox.search.base.mapToCore
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.common.RestrictedMapboxSearchAPI
import com.mapbox.search.common.metadata.ChildMetadata
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.parking.ParkingInfo
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

    /**
     * A list of cuisines served if this metadata belongs to a food-serving POI,
     * such as a café or restaurant.
     * Returns `null` for other types of POIs.
     */
    @IgnoredOnParcel
    public val cuisines: List<String>? = coreMetadata.cuisines

    /**
     * Parking information for POIs that represent parking facilities, e.g., parking lots,
     * garages, street parking etc.
     */
    @IgnoredOnParcel
    @MapboxExperimental
    @RestrictedMapboxSearchAPI
    public val parkingInfo: ParkingInfo? = coreMetadata.parkingInfo?.mapToPlatform()

    internal constructor(
        metadata: Map<String, String> = HashMap(),
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
        cuisines: List<String>? = null,
        parkingInfo: ParkingInfo? = null,
    ) : this(
        createCoreResultMetadata(
            reviewCount = reviewCount,
            phone = phone,
            website = website,
            avRating = averageRating,
            description = description,
            openHours = openHours?.mapToCore(),
            primaryPhoto = primaryPhotos?.map { it.mapToCore() },
            otherPhoto = otherPhotos?.map { it.mapToCore() },
            cpsJson = cpsJson,
            parking = parking?.mapToCore(),
            children = children?.map { it.mapToCore() },
            data = HashMap(metadata),
            wheelchairAccessible = wheelchairAccessible,
            delivery = delivery,
            driveThrough = driveThrough,
            reservable = reservable,
            parkingAvailable = parkingAvailable,
            valetParking = valetParking,
            streetParking = streetParking,
            servesBreakfast = servesBreakfast,
            servesBrunch = servesBrunch,
            servesDinner = servesDinner,
            servesLunch = servesLunch,
            servesWine = servesWine,
            servesBeer = servesBeer,
            takeout = takeout,
            facebookId = facebookId,
            fax = fax,
            email = email,
            instagram = instagram,
            twitter = twitter,
            priceLevel = priceLevel,
            servesVegan = servesVegan,
            servesVegetarian = servesVegetarian,
            rating = rating,
            popularity = popularity,
            // We don't support EV metadata for online search yet
            evMetadata = null,
            cuisines = cuisines,
            parkingInfo = parkingInfo?.mapToCore(),
        )
    )

    /**
     * Creates a [SearchResultMetadata.Builder] from this instance.
     *
     * @return [SearchResultMetadata.Builder] a builder from this instance
     */
    public fun toBuilder(): Builder {
        return Builder()
            .metadata(extraData)
            .reviewCount(reviewCount)
            .phone(phone)
            .website(website)
            .averageRating(averageRating)
            .description(description)
            .primaryPhotos(primaryPhotos)
            .otherPhotos(otherPhotos)
            .openHours(openHours)
            .parking(parking)
            .children(children)
            .cpsJson(cpsJson)
            .wheelchairAccessible(wheelchairAccessible)
            .delivery(delivery)
            .driveThrough(driveThrough)
            .reservable(reservable)
            .parkingAvailable(parkingAvailable)
            .valetParking(valetParking)
            .streetParking(streetParking)
            .servesBreakfast(servesBreakfast)
            .servesBrunch(servesBrunch)
            .servesDinner(servesDinner)
            .servesLunch(servesLunch)
            .servesWine(servesWine)
            .servesBeer(servesBeer)
            .takeout(takeout)
            .facebookId(facebookId)
            .fax(fax)
            .email(email)
            .instagram(instagram)
            .twitter(twitter)
            .priceLevel(priceLevel)
            .servesVegan(servesVegan)
            .servesVegetarian(servesVegetarian)
            .rating(rating)
            .popularity(popularity)
            .parkingInfo(parkingInfo)
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResultMetadata

        return coreMetadata == other.coreMetadata
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
                "popularity=$popularity, " +
                "cuisines=$cuisines, " +
                "parkingInfo=$parkingInfo" +
                ")"
    }

    /**
     * Builder for creating instances of [SearchResultMetadata].
     */
    public class Builder {

        private var metadata: Map<String, String> = HashMap()
        private var reviewCount: Int? = null
        private var phone: String? = null
        private var website: String? = null
        private var averageRating: Double? = null
        private var description: String? = null
        private var primaryPhotos: List<ImageInfo>? = null
        private var otherPhotos: List<ImageInfo>? = null
        private var openHours: OpenHours? = null
        private var parking: ParkingData? = null
        private var children: List<ChildMetadata>? = null
        private var cpsJson: String? = null
        private var wheelchairAccessible: Boolean? = null
        private var delivery: Boolean? = null
        private var driveThrough: Boolean? = null
        private var reservable: Boolean? = null
        private var parkingAvailable: Boolean? = null
        private var valetParking: Boolean? = null
        private var streetParking: Boolean? = null
        private var servesBreakfast: Boolean? = null
        private var servesBrunch: Boolean? = null
        private var servesDinner: Boolean? = null
        private var servesLunch: Boolean? = null
        private var servesWine: Boolean? = null
        private var servesBeer: Boolean? = null
        private var takeout: Boolean? = null
        private var facebookId: String? = null
        private var fax: String? = null
        private var email: String? = null
        private var instagram: String? = null
        private var twitter: String? = null
        private var priceLevel: String? = null
        private var servesVegan: Boolean? = null
        private var servesVegetarian: Boolean? = null
        private var rating: Float? = null
        private var popularity: Float? = null
        private var cuisines: List<String>? = null
        private var parkingInfo: ParkingInfo? = null

        /**
         * Sets the metadata for the search result.
         * @param metadata metadata.
         * @return The builder instance.
         */
        public fun metadata(metadata: Map<String, String>): Builder = apply { this.metadata = metadata }

        /**
         * Sets the review count for the search result.
         * @param reviewCount The number of reviews.
         * @return The builder instance.
         */
        public fun reviewCount(reviewCount: Int?): Builder = apply { this.reviewCount = reviewCount }

        /**
         * Sets the phone number associated with the search result.
         * @param phone The phone number.
         * @return The builder instance.
         */
        public fun phone(phone: String?): Builder = apply { this.phone = phone }

        /**
         * Sets the website associated with the search result.
         * @param website The website URL.
         * @return The builder instance.
         */
        public fun website(website: String?): Builder = apply { this.website = website }

        /**
         * Sets the average rating for the search result.
         * @param averageRating The average rating.
         * @return The builder instance.
         */
        public fun averageRating(averageRating: Double?): Builder = apply { this.averageRating = averageRating }

        /**
         * Sets the description of the search result.
         * @param description The description text.
         * @return The builder instance.
         */
        public fun description(description: String?): Builder = apply { this.description = description }

        /**
         * Sets the list of primary photos associated with the search result.
         * @param primaryPhotos The list of primary photos.
         * @return The builder instance.
         */
        public fun primaryPhotos(primaryPhotos: List<ImageInfo>?): Builder = apply { this.primaryPhotos = primaryPhotos }

        /**
         * Sets the list of other (non-primary) photos associated with the search result.
         * @param otherPhotos The list of other photos.
         * @return The builder instance.
         */
        public fun otherPhotos(otherPhotos: List<ImageInfo>?): Builder = apply { this.otherPhotos = otherPhotos }

        /**
         * Sets the open hours information for the search result.
         * @param openHours The open hours information.
         * @return The builder instance.
         */
        public fun openHours(openHours: OpenHours?): Builder = apply { this.openHours = openHours }

        /**
         * Sets the parking information for the search result.
         * @param parking The parking information.
         * @return The builder instance.
         */
        public fun parking(parking: ParkingData?): Builder = apply { this.parking = parking }

        /**
         * Sets the child metadata for points of interest associated with the search result.
         * @param children The list of child metadata.
         * @return The builder instance.
         */
        public fun children(children: List<ChildMetadata>?): Builder = apply { this.children = children }

        /**
         * Sets the CPS-specific metadata as a JSON string.
         * @param cpsJson The CPS metadata in JSON format.
         * @return The builder instance.
         */
        public fun cpsJson(cpsJson: String?): Builder = apply { this.cpsJson = cpsJson }

        /**
         * Sets whether the location is wheelchair accessible.
         * @param wheelchairAccessible True if wheelchair accessible, otherwise false.
         * @return The builder instance.
         */
        public fun wheelchairAccessible(wheelchairAccessible: Boolean?): Builder = apply { this.wheelchairAccessible = wheelchairAccessible }

        /**
         * Sets whether the location offers delivery services.
         * @param delivery True if delivery is offered, otherwise false.
         * @return The builder instance.
         */
        public fun delivery(delivery: Boolean?): Builder = apply { this.delivery = delivery }

        /**
         * Sets whether the location has a drive-through service.
         * @param driveThrough True if drive-through is available, otherwise false.
         * @return The builder instance.
         */
        public fun driveThrough(driveThrough: Boolean?): Builder = apply { this.driveThrough = driveThrough }

        /**
         * Sets whether the location accepts reservations.
         * @param reservable True if reservations are accepted, otherwise false.
         * @return The builder instance.
         */
        public fun reservable(reservable: Boolean?): Builder = apply { this.reservable = reservable }

        /**
         * Sets whether parking is available at the location.
         * @param parkingAvailable True if parking is available, otherwise false.
         * @return The builder instance.
         */
        public fun parkingAvailable(parkingAvailable: Boolean?): Builder = apply { this.parkingAvailable = parkingAvailable }

        /**
         * Sets whether valet parking is available at the location.
         * @param valetParking True if valet parking is available, otherwise false.
         * @return The builder instance.
         */
        public fun valetParking(valetParking: Boolean?): Builder = apply { this.valetParking = valetParking }

        /**
         * Sets whether street parking is available near the location.
         * @param streetParking True if street parking is available, otherwise false.
         * @return The builder instance.
         */
        public fun streetParking(streetParking: Boolean?): Builder = apply { this.streetParking = streetParking }

        /**
         * Sets whether the location serves breakfast.
         * @param servesBreakfast True if breakfast is served, otherwise false.
         * @return The builder instance.
         */
        public fun servesBreakfast(servesBreakfast: Boolean?): Builder = apply { this.servesBreakfast = servesBreakfast }

        /**
         * Sets whether the location serves brunch.
         * @param servesBrunch True if brunch is served, otherwise false.
         * @return The builder instance.
         */
        public fun servesBrunch(servesBrunch: Boolean?): Builder = apply { this.servesBrunch = servesBrunch }

        /**
         * Sets whether the location serves dinner.
         * @param servesDinner True if dinner is served, otherwise false.
         * @return The builder instance.
         */
        public fun servesDinner(servesDinner: Boolean?): Builder = apply { this.servesDinner = servesDinner }

        /**
         * Sets whether the location serves lunch.
         * @param servesLunch True if lunch is served, otherwise false.
         * @return The builder instance.
         */
        public fun servesLunch(servesLunch: Boolean?): Builder = apply { this.servesLunch = servesLunch }

        /**
         * Sets whether the location serves wine.
         * @param servesWine True if wine is served, otherwise false.
         * @return The builder instance.
         */
        public fun servesWine(servesWine: Boolean?): Builder = apply { this.servesWine = servesWine }

        /**
         * Sets whether the location serves beer.
         * @param servesBeer True if beer is served, otherwise false.
         * @return The builder instance.
         */
        public fun servesBeer(servesBeer: Boolean?): Builder = apply { this.servesBeer = servesBeer }

        /**
         * Sets whether takeout services are available at the location.
         * @param takeout True if takeout is available, otherwise false.
         * @return The builder instance.
         */
        public fun takeout(takeout: Boolean?): Builder = apply { this.takeout = takeout }

        /**
         * Sets the Facebook ID associated with the location.
         * @param facebookId The Facebook ID.
         * @return The builder instance.
         */
        public fun facebookId(facebookId: String?): Builder = apply { this.facebookId = facebookId }

        /**
         * Sets the fax number associated with the location.
         * @param fax The fax number.
         * @return The builder instance.
         */
        public fun fax(fax: String?): Builder = apply { this.fax = fax }

        /**
         * Sets the email address associated with the location.
         * @param email The email address.
         * @return The builder instance.
         */
        public fun email(email: String?): Builder = apply { this.email = email }

        /**
         * Sets the Instagram handle associated with the location.
         * @param instagram The Instagram handle.
         * @return The builder instance.
         */
        public fun instagram(instagram: String?): Builder = apply { this.instagram = instagram }

        /**
         * Sets the Twitter handle associated with the location.
         * @param twitter The Twitter handle.
         * @return The builder instance.
         */
        public fun twitter(twitter: String?): Builder = apply { this.twitter = twitter }

        /**
         * Sets the price level of the location.
         * @param priceLevel The price level, represented by a string of dollar signs ("$" to "$$$$").
         * @return The builder instance.
         */
        public fun priceLevel(priceLevel: String?): Builder = apply { this.priceLevel = priceLevel }

        /**
         * Sets whether vegan options are available at the location.
         * @param servesVegan True if vegan options are available, otherwise false.
         * @return The builder instance.
         */
        public fun servesVegan(servesVegan: Boolean?): Builder = apply { this.servesVegan = servesVegan }

        /**
         * Sets whether vegetarian options are available at the location.
         * @param servesVegetarian True if vegetarian options are available, otherwise false.
         * @return The builder instance.
         */
        public fun servesVegetarian(servesVegetarian: Boolean?): Builder = apply { this.servesVegetarian = servesVegetarian }

        /**
         * Sets the rating of the location, on a scale from 1 to 5.
         * @param rating The rating.
         * @return The builder instance.
         */
        public fun rating(rating: Float?): Builder = apply { this.rating = rating }

        /**
         * Sets the popularity score of the location, scaled from 0 to 1.
         * @param popularity The popularity score.
         * @return The builder instance.
         */
        public fun popularity(popularity: Float?): Builder = apply { this.popularity = popularity }

        /**
         * Sets a list of cuisines served if this metadata belongs to a food-serving POI,
         * such as a café or restaurant.
         * @param cuisines List of cuisines served.
         * @return The builder instance.
         */
        public fun cuisines(cuisines: List<String>?): Builder = apply { this.cuisines = cuisines }

        /**
         * Sets parking information for POIs that represent parking facilities, e.g., parking lots,
         * garages, street parking etc.
         */
        @MapboxExperimental
        public fun parkingInfo(parkingInfo: ParkingInfo?): Builder = apply {
            this.parkingInfo = parkingInfo
        }

        /**
         * Builds an instance of [SearchResultMetadata] using the provided values.
         * @return A new instance of [SearchResultMetadata].
         */
        public fun build(): SearchResultMetadata {
            return SearchResultMetadata(
                metadata = metadata,
                reviewCount = reviewCount,
                phone = phone,
                website = website,
                averageRating = averageRating,
                description = description,
                primaryPhotos = primaryPhotos,
                otherPhotos = otherPhotos,
                openHours = openHours,
                parking = parking,
                children = children,
                cpsJson = cpsJson,
                wheelchairAccessible = wheelchairAccessible,
                delivery = delivery,
                driveThrough = driveThrough,
                reservable = reservable,
                parkingAvailable = parkingAvailable,
                valetParking = valetParking,
                streetParking = streetParking,
                servesBreakfast = servesBreakfast,
                servesBrunch = servesBrunch,
                servesDinner = servesDinner,
                servesLunch = servesLunch,
                servesWine = servesWine,
                servesBeer = servesBeer,
                takeout = takeout,
                facebookId = facebookId,
                fax = fax,
                email = email,
                instagram = instagram,
                twitter = twitter,
                priceLevel = priceLevel,
                servesVegan = servesVegan,
                servesVegetarian = servesVegetarian,
                rating = rating,
                popularity = popularity,
                cuisines = cuisines,
                parkingInfo = parkingInfo,
            )
        }
    }
}
