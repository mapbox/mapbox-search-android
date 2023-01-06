package com.mapbox.search

import android.os.Parcelable
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.Reserved.Flags.SBS
import com.mapbox.search.RouteOptions.Deviation.SarType
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.IsoCountry
import com.mapbox.search.common.IsoLanguage
import kotlinx.parcelize.Parcelize

/**
 * Search options, used for forward geocoding.
 * @see SearchEngine
 */
@Parcelize
public class SearchOptions @JvmOverloads public constructor(

    /**
     * Bias the response to favor results that are closer to this location, provided as Point.
     */
    public val proximity: Point? = null,

    /**
     * Limit results to only those contained within the supplied bounding box.
     * The bounding box cannot cross the 180th meridian.
     */
    public val boundingBox: BoundingBox? = null,

    /**
     * Limit results to one or more countries.
     */
    public val countries: List<IsoCountry>? = null,

    /**
     * Specify whether the Geocoding API should attempt approximate, as well as exact, matching when performing searches (true, default), or whether it should opt out of this behavior and only attempt exact matching (false). For example, the default setting might return Washington, DC for a query of Washington, even though the query was misspelled.
     *
     * Note: Supported for Geocoding API only.
     */
    public val fuzzyMatch: Boolean? = null,

    /**
     * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
     * If language is not set explicitly, then language from default system locale will be used.
     *
     * Note: Geocoding API supports a few languages, Single Box Search – only one.
     */
    public val languages: List<IsoLanguage>? = defaultSearchOptionsLanguage(),

    /**
     * Specify the maximum number of results to return, including results from [com.mapbox.search.record.IndexableDataProvider].
     * The maximum number of search results returned from the server is 10.
     */
    public val limit: Int? = null,

    /**
     * Filter results to include only a subset (one or more) of the available feature types. Options are country, region, postcode, district, place, locality, neighborhood, address, and poi.
     */
    public val types: List<QueryType>? = null,

    /**
     * Request debounce value in milliseconds. Previous request will be cancelled if the new one made within specified by [requestDebounce] time interval.
     */
    public val requestDebounce: Int? = null,

    /**
     * Point for ETA calculation from it to search result.
     *
     * Note: Supported for Single Box Search API only. Reserved for internal and special use.
     */
    @Reserved(SBS)
    public val origin: Point? = null,

    /**
     * Navigation options used for proper calculation of ETA and results ranking.
     *
     * Note: Supported for Single Box Search API only. Reserved for internal and special use.
     */
    @Reserved(SBS)
    public val navigationOptions: SearchNavigationOptions? = null,

    /**
     * Options to configure Route for search along the route functionality.
     *
     * Note: Supported for Single Box Search API only. Reserved for internal and special use.
     */
    @Reserved(SBS)
    public val routeOptions: RouteOptions? = null,

    /**
     * Non-verified query parameters, that will be added to the server API request.
     *
     * Note: Incorrect usage of this parameter may cause failed or malformed response. Do not use it without SDK developers agreement.
     */
    public val unsafeParameters: Map<String, String>? = null,

    /**
     * Specify whether to ignore [com.mapbox.search.record.IndexableRecord] results or not, default is false.
     * When search by [com.mapbox.search.record.IndexableRecord] is enabled, the results can be matched only by
     * query string, while other parameters like [countries], [languages], [unsafeParameters]
     * (and some others) can be ignored.
     */
    public val ignoreIndexableRecords: Boolean = false,

    /**
     * Allows to look up for indexable records only within specified distance threshold,
     * i.e. within a circle with this radius.
     * Threshold specified in meters.
     */
    public val indexableRecordsDistanceThresholdMeters: Double? = null,
) : Parcelable {

    init {
        require(limit == null || limit > 0) { "'limit' should be greater than 0 (passed value: $limit)." }
        require(indexableRecordsDistanceThresholdMeters == null || indexableRecordsDistanceThresholdMeters.compareTo(0.0) >= 0) {
            "'indexableRecordsDistanceThresholdMeters' can't be negative (passed value: $indexableRecordsDistanceThresholdMeters)"
        }
    }

    /**
     * Creates new [SearchOptions] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        proximity: Point? = this.proximity,
        boundingBox: BoundingBox? = this.boundingBox,
        countries: List<IsoCountry>? = this.countries,
        fuzzyMatch: Boolean? = this.fuzzyMatch,
        languages: List<IsoLanguage>? = this.languages,
        limit: Int? = this.limit,
        types: List<QueryType>? = this.types,
        requestDebounce: Int? = this.requestDebounce,
        origin: Point? = this.origin,
        navigationOptions: SearchNavigationOptions? = this.navigationOptions,
        routeOptions: RouteOptions? = this.routeOptions,
        unsafeParameters: Map<String, String>? = this.unsafeParameters,
        ignoreIndexableRecords: Boolean = this.ignoreIndexableRecords,
        indexableRecordsDistanceThresholdMeters: Double? = this.indexableRecordsDistanceThresholdMeters,
    ): SearchOptions {
        return SearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            fuzzyMatch = fuzzyMatch,
            languages = languages,
            limit = limit,
            types = types,
            requestDebounce = requestDebounce,
            origin = origin,
            navigationOptions = navigationOptions,
            routeOptions = routeOptions,
            unsafeParameters = unsafeParameters,
            ignoreIndexableRecords = ignoreIndexableRecords,
            indexableRecordsDistanceThresholdMeters = indexableRecordsDistanceThresholdMeters,
        )
    }

    /**
     * Creates new [SearchOptions.Builder] from current [SearchOptions] instance.
     */
    public fun toBuilder(): Builder {
        return Builder(this)
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchOptions

        if (proximity != other.proximity) return false
        if (boundingBox != other.boundingBox) return false
        if (countries != other.countries) return false
        if (fuzzyMatch != other.fuzzyMatch) return false
        if (languages != other.languages) return false
        if (limit != other.limit) return false
        if (types != other.types) return false
        if (requestDebounce != other.requestDebounce) return false
        if (origin != other.origin) return false
        if (navigationOptions != other.navigationOptions) return false
        if (routeOptions != other.routeOptions) return false
        if (unsafeParameters != other.unsafeParameters) return false
        if (ignoreIndexableRecords != other.ignoreIndexableRecords) return false
        if (!indexableRecordsDistanceThresholdMeters.safeCompareTo(other.indexableRecordsDistanceThresholdMeters)) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = proximity?.hashCode() ?: 0
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (countries?.hashCode() ?: 0)
        result = 31 * result + (fuzzyMatch?.hashCode() ?: 0)
        result = 31 * result + (languages?.hashCode() ?: 0)
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (types?.hashCode() ?: 0)
        result = 31 * result + (requestDebounce ?: 0)
        result = 31 * result + (origin?.hashCode() ?: 0)
        result = 31 * result + (navigationOptions?.hashCode() ?: 0)
        result = 31 * result + (routeOptions?.hashCode() ?: 0)
        result = 31 * result + (unsafeParameters?.hashCode() ?: 0)
        result = 31 * result + ignoreIndexableRecords.hashCode()
        result = 31 * result + indexableRecordsDistanceThresholdMeters.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchOptions(" +
                "proximity=$proximity, " +
                "boundingBox=$boundingBox, " +
                "countries=$countries, " +
                "fuzzyMatch=$fuzzyMatch, " +
                "languages=$languages, " +
                "limit=$limit, " +
                "types=$types, " +
                "requestDebounce=$requestDebounce, " +
                "origin=$origin, " +
                "navigationOptions=$navigationOptions, " +
                "routeOptions=$routeOptions, " +
                "unsafeParameters=$unsafeParameters, " +
                "ignoreIndexableRecords=$ignoreIndexableRecords, " +
                "indexableRecordsDistanceThresholdMeters=$indexableRecordsDistanceThresholdMeters" +
                ")"
    }

    /**
     * Builder for comfortable creation of [SearchOptions] instance.
     */
    @Suppress("TooManyFunctions")
    public class Builder() {

        private var proximity: Point? = null
        private var boundingBox: BoundingBox? = null
        private var countries: List<IsoCountry>? = null
        private var fuzzyMatch: Boolean? = null
        private var languages: List<IsoLanguage>? = defaultSearchOptionsLanguage()
        private var limit: Int? = null
        private var types: List<QueryType>? = null
        private var requestDebounce: Int? = null
        private var origin: Point? = null
        private var navigationOptions: SearchNavigationOptions? = null
        private var routeOptions: RouteOptions? = null
        private var unsafeParameters: Map<String, String>? = null
        private var ignoreIndexableRecords: Boolean = false
        private var indexableRecordsDistanceThresholdMeters: Double? = null

        internal constructor(options: SearchOptions) : this() {
            proximity = options.proximity
            boundingBox = options.boundingBox
            countries = options.countries
            fuzzyMatch = options.fuzzyMatch
            languages = options.languages
            limit = options.limit
            types = options.types
            requestDebounce = options.requestDebounce
            origin = options.origin
            navigationOptions = options.navigationOptions
            routeOptions = options.routeOptions
            unsafeParameters = options.unsafeParameters
            ignoreIndexableRecords = options.ignoreIndexableRecords
            indexableRecordsDistanceThresholdMeters = options.indexableRecordsDistanceThresholdMeters
        }

        /**
         * Bias the response to favor results that are closer to this location, provided as Point class instance.
         */
        public fun proximity(proximity: Point?): Builder = apply { this.proximity = proximity }

        /**
         * Limit results to only those contained within the supplied bounding box. The bounding box cannot cross the 180th meridian.
         */
        public fun boundingBox(boundingBox: BoundingBox): Builder = apply { this.boundingBox = boundingBox }

        /**
         * Limit results to one or more countries.
         */
        public fun countries(vararg countries: IsoCountry): Builder = apply { this.countries = countries.toList() }

        /**
         * Limit results to one or more countries.
         */
        public fun countries(countries: List<IsoCountry>): Builder = apply { this.countries = countries }

        /**
         * Specify whether the Geocoding API should attempt approximate, as well as exact, matching when performing searches (true, default), or whether it should opt out of this behavior and only attempt exact matching (false). For example, the default setting might return Washington, DC for a query of Washington, even though the query was misspelled.
         *
         * Note: Supported for Geocoding API only.
         */
        public fun fuzzyMatch(fuzzyMatch: Boolean): Builder = apply { this.fuzzyMatch = fuzzyMatch }

        /**
         * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
         * If language is not set explicitly, then language from default system locale will be used.
         *
         * Note: Geocoding API supports a few languages, Single Box Search – only one.
         */
        public fun languages(vararg languages: IsoLanguage): Builder = apply { this.languages = languages.toList() }

        /**
         * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
         * If language is not set explicitly, then language from default system locale will be used.
         *
         * Note: Geocoding API supports a few languages, Single Box Search – only one.
         */
        public fun languages(languages: List<IsoLanguage>): Builder = apply { this.languages = languages }

        /**
         * Specify the maximum number of results to return. The maximum supported is 10.
         */
        public fun limit(limit: Int): Builder = apply { this.limit = limit }

        /**
         * Filter results to include only a subset (one or more) of the available feature types. Options are country, region, postcode, district, place, locality, neighborhood, address, and poi.
         */
        public fun types(vararg types: QueryType): Builder = apply { this.types = types.toList() }

        /**
         * Filter results to include only a subset (one or more) of the available feature types. Options are country, region, postcode, district, place, locality, neighborhood, address, and poi.
         */
        public fun types(types: List<QueryType>): Builder = apply { this.types = types }

        /**
         * Request debounce value in milliseconds. Previous request will be cancelled if the new one made within specified by [requestDebounce] time interval.
         */
        public fun requestDebounce(debounce: Int): Builder = apply { this.requestDebounce = debounce }

        /**
         * Point for ETA calculation from it to search result.
         *
         * Note: Supported for Single Box Search API only. Reserved for internal and special use.
         */
        @Reserved(SBS)
        public fun origin(origin: Point): Builder = apply { this.origin = origin }

        /**
         * Navigation options used for proper calculation of ETA and results ranking.
         *
         * Note: Supported for Single Box Search API only. Reserved for internal and special use.
         */
        @Reserved(SBS)
        public fun navigationOptions(navigationOptions: SearchNavigationOptions): Builder = apply {
            this.navigationOptions = navigationOptions
        }

        /**
         * Options to configure Route for search along the route functionality.
         *
         * Note: Supported for Single Box Search API only. Reserved for internal and special use.
         */
        @Reserved(SBS)
        public fun routeOptions(routeOptions: RouteOptions): Builder = apply { this.routeOptions = routeOptions }

        /**
         * Non-verified query parameters, that will be added to the server API request.
         *
         * Note: Incorrect usage of this parameter may cause failed or malformed response. Do not use it without SDK developers agreement.
         */
        public fun unsafeParameters(unsafeParameters: Map<String, String>): Builder = apply {
            this.unsafeParameters = unsafeParameters
        }

        /**
         * Non-verified query parameters, that will be added to the server API request.
         *
         * Note: Incorrect usage of this parameter may cause failed or malformed response. Do not use it without SDK developers agreement.
         */
        public fun ignoreIndexableRecords(ignoreIndexableRecords: Boolean): Builder = apply {
            this.ignoreIndexableRecords = ignoreIndexableRecords
        }

        /**
         * Allows to look up for indexable records only within specified distance threshold,
         * i.e. within a circle with this radius.
         * Threshold specified in meters.
         */
        public fun indexableRecordsDistanceThresholdMeters(threshold: Double?): Builder = apply {
            this.indexableRecordsDistanceThresholdMeters = threshold
        }

        /**
         * Create [SearchOptions] instance from builder data.
         */
        public fun build(): SearchOptions = SearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            fuzzyMatch = fuzzyMatch,
            languages = languages,
            limit = limit,
            types = types,
            requestDebounce = requestDebounce,
            origin = origin,
            navigationOptions = navigationOptions,
            routeOptions = routeOptions,
            unsafeParameters = unsafeParameters,
            ignoreIndexableRecords = ignoreIndexableRecords,
            indexableRecordsDistanceThresholdMeters = indexableRecordsDistanceThresholdMeters,
        )
    }
}

@JvmSynthetic
internal fun defaultSearchOptionsLanguage(): List<IsoLanguage> {
    return listOf(defaultLocaleLanguage())
}

@JvmSynthetic
internal fun validateLimit(limit: Int?): Int? {
    if (limit != null && limit <= 0) {
        assertDebug(false) { "Provided limit should be greater than 0 (was found: $limit)." }
        return null
    }
    return limit
}

@JvmSynthetic
internal fun SearchOptions.mapToCore(): CoreSearchOptions = CoreSearchOptions(
    proximity,
    origin,
    navigationOptions?.navigationProfile?.rawName,
    navigationOptions?.etaType?.rawName,
    boundingBox?.mapToCore(),
    countries?.map { it.code },
    fuzzyMatch,
    languages?.map { it.code },
    limit,
    types?.mapToCoreTypes(),
    ignoreIndexableRecords,
    indexableRecordsDistanceThresholdMeters,
    requestDebounce,
    routeOptions?.route,
    routeOptions?.deviation?.sarType?.rawName,
    routeOptions?.timeDeviationMinutes,
    unsafeParameters?.let { (it as? HashMap) ?: HashMap(it) },
)

@JvmSynthetic
internal fun CoreSearchOptions.mapToPlatform(): SearchOptions = SearchOptions(
    proximity = proximity,
    boundingBox = bbox?.mapToPlatform(),
    countries = countries?.map { IsoCountry(it) },
    fuzzyMatch = @Suppress("DEPRECATION") fuzzyMatch,
    languages = language?.map { IsoLanguage(it) },
    limit = validateLimit(limit),
    types = types?.mapToPlatformTypes(),
    requestDebounce = requestDebounce,
    origin = origin,
    navigationOptions = navProfile?.let {
        SearchNavigationOptions(
            navigationProfile = SearchNavigationProfile(it),
            etaType = etaType?.let(::EtaType),
        )
    },
    routeOptions = if (route != null && timeDeviation != null) {
        RouteOptions(
            route = route!!,
            deviation = RouteOptions.Deviation.Time.fromMinutes(
                minutes = timeDeviation!!,
                sarType = sarType?.let(::SarType),
            ),
        )
    } else null,
    unsafeParameters = addonAPI,
    ignoreIndexableRecords = ignoreUR,
    indexableRecordsDistanceThresholdMeters = urDistanceThreshold,
)
