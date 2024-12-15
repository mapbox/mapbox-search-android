package com.mapbox.search

import android.os.Parcelable
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.Reserved.Flags.SBS
import com.mapbox.search.Reserved.Flags.SEARCH_BOX
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import kotlinx.parcelize.Parcelize

/**
 * Search options, used for category search.
 * @see SearchEngine
 */
@Parcelize
public class CategorySearchOptions @JvmOverloads public constructor(

    /**
     * Bias the response to favor results that are closer to this location, provided as Point.
     */
    public val proximity: Point? = null,

    /**
     * Limit results to only those contained within the supplied bounding box. The bounding box cannot cross the 180th meridian.
     */
    public val boundingBox: BoundingBox? = null,

    /**
     * Limit results to one or more countries.
     */
    public val countries: List<IsoCountryCode>? = null,

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
    public val languages: List<IsoLanguageCode>? = defaultSearchOptionsLanguage(),

    /**
     * Specify the maximum number of results to return, including results from [com.mapbox.search.record.IndexableDataProvider].
     * The maximum number of search results is determined by server. For example,
     * for the [ApiType.SEARCH_BOX] the maximum number of results to return is 25.
     */
    public val limit: Int? = null,

    /**
     * Request debounce value in milliseconds. Previous request will be cancelled if the new one made within specified by [requestDebounce] time interval.
     */
    public val requestDebounce: Int? = null,

    /**
     * Point for alternative search ranking logic, that is turned on if [navigationProfile] is specified.
     *
     * Note: Supported for Single Box Search and Search Box APIs only.
     *
     * @see navigationProfile
     */
    @Reserved(SBS, SEARCH_BOX)
    public val origin: Point? = null,

    /**
     * Type of movement. Used to alter search ranking logic: the faster you can walk/drive from the [origin] to the search result, the higher search result rank.
     *
     * Note: Supported for Single Box Search and Search Box APIs only.
     *
     * Deprecated, use [navigationOptions] property instead. In case both
     * [navigationProfile] and [navigationOptions] are provided, parameters from [navigationOptions]
     * will be taken.
     *
     * @see navigationOptions
     */
    @Reserved(SBS, SEARCH_BOX)
    @Deprecated("Use navigationOptions property instead", ReplaceWith("navigationOptions"))
    public val navigationProfile: NavigationProfile? = null,

    /**
     * Options to configure Route for search along the route functionality.
     *
     * Note: Supported for Single Box Search and Search Box APIs only.
     */
    @Reserved(SBS, SEARCH_BOX)
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

    /**
     * When set to true and multiple categories are requested, e.g.
     * `SearchEngine.search(listOf("coffee_shop", "hotel"), ...)`,
     * results will include at least one POI for each category, provided a POI is available
     * in a nearby location.
     *
     * A comma-separated list of multiple category values in the request determines the sort order
     * of the POI result. For example, for request
     * `SearchEngine.search(listOf("coffee_shop", "hotel"), ...)`, coffee_shop POI will be listed
     * first in the results.
     *
     * If there is more than one POI for categories, the number of search results will include
     * multiple features for each category. For example, assuming that
     * restaurant, coffee, parking_lot categories are requested and limit parameter is 10,
     * the result will be ranked as follows:
     * - 1st to 4th: restaurant POIs
     * - 5th to 7th: coffee POIs
     * - 8th to 10th: parking_lot POI
     */
    @Reserved(SEARCH_BOX)
    public val ensureResultsPerCategory: Boolean? = null,

    /**
     * Request additional metadata attributes besides the basic ones.
     */
    @Reserved(SEARCH_BOX)
    public val attributeSets: List<AttributeSet>? = null,

    /**
     * Navigation options used for proper calculation of ETA and results ranking.
     *
     * Note: Supported for Single Box Search and Search Box APIs only.
     *
     * This property replaces old [navigationProfile].In case both
     * [navigationProfile] and [navigationOptions] are provided, parameters from [navigationOptions]
     * will be taken.
     */
    @Reserved(SEARCH_BOX)
    public val navigationOptions: SearchNavigationOptions? = null,
) : Parcelable {

    init {
        require(limit == null || limit > 0) { "'limit' should be greater than 0 (passed value: $limit)." }
        require(indexableRecordsDistanceThresholdMeters == null || indexableRecordsDistanceThresholdMeters.compareTo(0.0) >= 0) {
            "'indexableRecordsDistanceThresholdMeters' can't be negative (passed value: $indexableRecordsDistanceThresholdMeters)"
        }
    }

    /**
     * Creates new [CategorySearchOptions] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        proximity: Point? = this.proximity,
        boundingBox: BoundingBox? = this.boundingBox,
        countries: List<IsoCountryCode>? = this.countries,
        fuzzyMatch: Boolean? = this.fuzzyMatch,
        languages: List<IsoLanguageCode>? = this.languages,
        limit: Int? = this.limit,
        requestDebounce: Int? = this.requestDebounce,
        origin: Point? = this.origin,
        navigationProfile: NavigationProfile? = this.navigationProfile,
        routeOptions: RouteOptions? = this.routeOptions,
        unsafeParameters: Map<String, String>? = this.unsafeParameters,
        ignoreIndexableRecords: Boolean = this.ignoreIndexableRecords,
        indexableRecordsDistanceThresholdMeters: Double? = this.indexableRecordsDistanceThresholdMeters,
        ensureResultsPerCategory: Boolean? = this.ensureResultsPerCategory,
        attributeSets: List<AttributeSet>? = this.attributeSets,
        navigationOptions: SearchNavigationOptions? = this.navigationOptions,
    ): CategorySearchOptions {
        return CategorySearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            fuzzyMatch = fuzzyMatch,
            languages = languages,
            limit = limit,
            requestDebounce = requestDebounce,
            origin = origin,
            navigationProfile = navigationProfile,
            routeOptions = routeOptions,
            unsafeParameters = unsafeParameters,
            ignoreIndexableRecords = ignoreIndexableRecords,
            indexableRecordsDistanceThresholdMeters = indexableRecordsDistanceThresholdMeters,
            ensureResultsPerCategory = ensureResultsPerCategory,
            attributeSets = attributeSets,
            navigationOptions = navigationOptions,
        )
    }

    /**
     * Creates new [CategorySearchOptions.Builder] from current [CategorySearchOptions] instance.
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

        other as CategorySearchOptions

        if (proximity != other.proximity) return false
        if (boundingBox != other.boundingBox) return false
        if (countries != other.countries) return false
        if (fuzzyMatch != other.fuzzyMatch) return false
        if (languages != other.languages) return false
        if (limit != other.limit) return false
        if (requestDebounce != other.requestDebounce) return false
        if (origin != other.origin) return false
        if (navigationProfile != other.navigationProfile) return false
        if (routeOptions != other.routeOptions) return false
        if (unsafeParameters != other.unsafeParameters) return false
        if (ignoreIndexableRecords != other.ignoreIndexableRecords) return false
        if (!indexableRecordsDistanceThresholdMeters.safeCompareTo(other.indexableRecordsDistanceThresholdMeters)) return false
        if (ensureResultsPerCategory != other.ensureResultsPerCategory) return false
        if (attributeSets != other.attributeSets) return false
        if (navigationOptions != other.navigationOptions) return false

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
        result = 31 * result + (requestDebounce ?: 0)
        result = 31 * result + (origin?.hashCode() ?: 0)
        result = 31 * result + (navigationProfile?.hashCode() ?: 0)
        result = 31 * result + (routeOptions?.hashCode() ?: 0)
        result = 31 * result + (unsafeParameters?.hashCode() ?: 0)
        result = 31 * result + ignoreIndexableRecords.hashCode()
        result = 31 * result + indexableRecordsDistanceThresholdMeters.hashCode()
        result = 31 * result + ensureResultsPerCategory.hashCode()
        result = 31 * result + attributeSets.hashCode()
        result = 31 * result + navigationOptions.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "CategorySearchOptions(" +
                "proximity=$proximity, " +
                "boundingBox=$boundingBox, " +
                "countries=$countries, " +
                "fuzzyMatch=$fuzzyMatch, " +
                "languages=$languages, " +
                "limit=$limit, " +
                "requestDebounce=$requestDebounce, " +
                "origin=$origin, " +
                "navigationProfile=$navigationProfile, " +
                "routeOptions=$routeOptions, " +
                "unsafeParameters=$unsafeParameters, " +
                "ignoreIndexableRecords=$ignoreIndexableRecords, " +
                "indexableRecordsDistanceThresholdMeters=$indexableRecordsDistanceThresholdMeters, " +
                "ensureResultsPerCategory=$ensureResultsPerCategory, " +
                "attributeSets=$attributeSets, " +
                "navigationOptions=$navigationOptions" +
                ")"
    }

    /**
     * Builder for comfortable creation of [CategorySearchOptions] instance.
     */
    @Suppress("TooManyFunctions")
    public class Builder() {

        private var proximity: Point? = null
        private var boundingBox: BoundingBox? = null
        private var countries: List<IsoCountryCode>? = null
        private var fuzzyMatch: Boolean? = null
        private var languages: List<IsoLanguageCode>? = defaultSearchOptionsLanguage()
        private var limit: Int? = null
        private var requestDebounce: Int? = null
        private var origin: Point? = null
        private var navigationProfile: NavigationProfile? = null
        private var routeOptions: RouteOptions? = null
        private var unsafeParameters: Map<String, String>? = null
        private var ignoreIndexableRecords: Boolean = false
        private var indexableRecordsDistanceThresholdMeters: Double? = null
        private var ensureResultsPerCategory: Boolean? = null
        private var attributeSets: List<AttributeSet>? = null
        private var navigationOptions: SearchNavigationOptions? = null

        internal constructor(options: CategorySearchOptions) : this() {
            proximity = options.proximity
            boundingBox = options.boundingBox
            countries = options.countries
            fuzzyMatch = @Suppress("DEPRECATION") options.fuzzyMatch
            languages = options.languages
            limit = options.limit
            requestDebounce = options.requestDebounce
            origin = options.origin
            navigationProfile = options.navigationProfile
            routeOptions = options.routeOptions
            unsafeParameters = options.unsafeParameters
            ignoreIndexableRecords = options.ignoreIndexableRecords
            indexableRecordsDistanceThresholdMeters = options.indexableRecordsDistanceThresholdMeters
            ensureResultsPerCategory = options.ensureResultsPerCategory
            attributeSets = options.attributeSets
            navigationOptions = options.navigationOptions
        }

        /**
         * Bias the response to favor results that are closer to this location, provided as Point.
         */
        public fun proximity(proximity: Point?): Builder = apply { this.proximity = proximity }

        /**
         * Limit results to only those contained within the supplied bounding box. The bounding box cannot cross the 180th meridian.
         */
        public fun boundingBox(boundingBox: BoundingBox): Builder = apply { this.boundingBox = boundingBox }

        /**
         * Limit results to one or more countries.
         */
        public fun countries(vararg countries: IsoCountryCode): Builder = apply { this.countries = countries.toList() }

        /**
         * Limit results to one or more countries.
         */
        public fun countries(countries: List<IsoCountryCode>): Builder = apply { this.countries = countries }

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
        public fun languages(vararg languages: IsoLanguageCode): Builder = apply { this.languages = languages.toList() }

        /**
         * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
         * If language is not set explicitly, then language from default system locale will be used.
         *
         * Note: Geocoding API supports a few languages, Single Box Search – only one.
         */
        public fun languages(languages: List<IsoLanguageCode>): Builder = apply { this.languages = languages }

        /**
         * Specify the maximum number of results to return, including results from [com.mapbox.search.record.IndexableDataProvider].
         * The maximum number of search results is determined by server. For example,
         * for the [ApiType.SEARCH_BOX] the maximum number of results to return is 25.
         */
        public fun limit(limit: Int): Builder = apply { this.limit = limit }

        /**
         * Request debounce value in milliseconds. Previous request will be cancelled if the new one made within specified by [requestDebounce] time interval.
         */
        public fun requestDebounce(debounce: Int): Builder = apply { this.requestDebounce = debounce }

        /**
         * Point for alternative search ranking logic, that is turned on if [navigationProfile] is specified.
         *
         * Note: Supported for Single Box Search API only.
         *
         * @see navigationProfile
         */
        @Reserved(SBS, SEARCH_BOX)
        public fun origin(origin: Point): Builder = apply { this.origin = origin }

        /**
         * Type of movement. Used to alter search ranking logic: the faster you can walk/drive from the [origin] to the search result, the higher search result rank.
         *
         * Note: Supported for Single Box Search API only.
         *
         * Deprecated, use [navigationOptions] property instead. In case both
         * [navigationProfile] and [navigationOptions] are provided, parameters from [navigationOptions]
         * will be taken.
         */
        @Reserved(SBS, SEARCH_BOX)
        @Deprecated("Use navigationOptions function instead", ReplaceWith("navigationOptions"))
        public fun navigationProfile(navigationProfile: NavigationProfile): Builder = apply {
            this.navigationProfile = navigationProfile
        }

        /**
         * Options to configure Route for search along the route functionality.
         *
         * Note: Supported for Single Box Search API only.
         */
        @Reserved(SBS, SEARCH_BOX)
        public fun routeOptions(routeOptions: RouteOptions): Builder = apply { this.routeOptions = routeOptions }

        /**
         * Non-verified query parameters, that will be added to the server API request.
         *
         * Note: Incorrect usage of this parameter may cause failed or malformed response. Do not use it without SDK developers agreement.
         */
        public fun unsafeParameters(unsafeParameters: Map<String, String>): Builder = apply { this.unsafeParameters = unsafeParameters }

        /**
         * Specify whether to ignore [com.mapbox.search.record.IndexableRecord] results or not, default is false.
         * When search by [com.mapbox.search.record.IndexableRecord] is enabled, the results can be matched only by
         * query string, while other parameters like [countries], [languages], [unsafeParameters]
         * (and some others) can be ignored.
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
         * When set to true and multiple categories are requested, e.g.
         * `SearchEngine.search(listOf("coffee_shop", "hotel"), ...)`,
         * results will include at least one POI for each category, provided a POI is available
         * in a nearby location.
         *
         * A comma-separated list of multiple category values in the request determines the sort order
         * of the POI result. For example, for request
         * `SearchEngine.search(listOf("coffee_shop", "hotel"), ...)`, coffee_shop POI will be listed
         * first in the results.
         *
         * If there is more than one POI for categories, the number of search results will include
         * multiple features for each category. For example, assuming that
         * restaurant, coffee, parking_lot categories are requested and limit parameter is 10,
         * the result will be ranked as follows:
         * - 1st to 4th: restaurant POIs
         * - 5th to 7th: coffee POIs
         * - 8th to 10th: parking_lot POI
         */
        @Reserved(SEARCH_BOX)
        public fun ensureResultsPerCategory(ensureResultsPerCategory: Boolean?): Builder = apply {
            this.ensureResultsPerCategory = ensureResultsPerCategory
        }

        /**
         * Request additional metadata attributes besides the basic ones.
         */
        @Reserved(SEARCH_BOX)
        public fun attributeSets(attributeSets: List<AttributeSet>?): Builder = apply {
            this.attributeSets = attributeSets
        }

        /**
         * Navigation options used for proper calculation of ETA and results ranking.
         *
         * Note: Supported for Single Box Search and Search Box APIs only.
         *
         * This property replaces old [navigationProfile].In case both
         * [navigationProfile] and [navigationOptions] are provided, parameters from [navigationOptions]
         * will be taken.
         */
        @Reserved(SEARCH_BOX)
        public fun navigationOptions(navigationOptions: SearchNavigationOptions?): Builder = apply {
            this.navigationOptions = navigationOptions
        }

        /**
         * Create [CategorySearchOptions] instance from builder data.
         */
        public fun build(): CategorySearchOptions = CategorySearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            fuzzyMatch = fuzzyMatch,
            languages = languages,
            limit = limit,
            requestDebounce = requestDebounce,
            origin = origin,
            navigationProfile = navigationProfile,
            routeOptions = routeOptions,
            unsafeParameters = unsafeParameters,
            ignoreIndexableRecords = ignoreIndexableRecords,
            indexableRecordsDistanceThresholdMeters = indexableRecordsDistanceThresholdMeters,
            ensureResultsPerCategory = ensureResultsPerCategory,
            attributeSets = attributeSets,
            navigationOptions = navigationOptions,
        )
    }
}

@JvmSynthetic
internal fun CategorySearchOptions.mapToCoreCategory(): CoreSearchOptions = createCoreSearchOptions(
    proximity = proximity,
    origin = origin,
    navProfile = navigationOptions?.navigationProfile?.rawName ?: navigationProfile?.rawName,
    etaType = navigationOptions?.etaType?.rawName,
    bbox = boundingBox?.mapToCore(),
    countries = countries?.map { it.code },
    fuzzyMatch = fuzzyMatch,
    language = languages?.map { it.code },
    limit = limit,
    ignoreUR = ignoreIndexableRecords,
    urDistanceThreshold = indexableRecordsDistanceThresholdMeters,
    requestDebounce = requestDebounce,
    route = routeOptions?.route,
    sarType = routeOptions?.deviation?.sarType?.rawName,
    timeDeviation = routeOptions?.timeDeviationMinutes,
    addonAPI = unsafeParameters?.let { (it as? HashMap) ?: HashMap(it) },
    ensureResultsPerCategory = ensureResultsPerCategory,
    attributeSets = attributeSets?.map { it.mapToCore() },
)
