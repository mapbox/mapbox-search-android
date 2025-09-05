package com.mapbox.search

import android.os.Parcelable
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.Reserved.Flags.SEARCH_BOX
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import kotlinx.parcelize.Parcelize

/**
 * Search options, used for the "Text Search".
 * @see SearchEngine.forward
 */
@Reserved(SEARCH_BOX)
@Parcelize
public class ForwardSearchOptions private constructor(

    /**
     * Specify the user’s language. This parameter controls the language of the text
     * supplied in responses, and also affects result scoring, with results matching
     * the user’s query in the requested language being preferred over results that
     * match in another language. For example, an autocomplete query for things that start with
     * Frank might return Frankfurt as the first result with an English (en) language parameter,
     * but Frankreich (“France”) with a German (de) language parameter.
     * If language is not set explicitly, then language from default system locale will be used.
     */
    public val language: IsoLanguageCode? = defaultLocaleLanguage(),

    /**
     * Specify the maximum number of results to return,
     * including results from [com.mapbox.search.record.IndexableDataProvider].
     * The maximum number of search results returned from the server is 10.
     */
    public val limit: Int? = null,

    /**
     * Bias the response to favor results that are closer to a specific location.
     * If not provided, the SDK will try to get current location from the
     * [SearchEngineSettings.locationProvider]. If location is not available,
     * the server we use IP as a proximity.
     *
     * When both [proximity] and [origin] are provided, [origin] is interpreted as the target
     * of a route, while proximity indicates the current user location.
     */
    public val proximity: Point? = null,

    /**
     * Limit results to only those contained within the supplied bounding box.
     * The bounding box cannot cross the 180th meridian (longitude +/-180.0 deg.)
     * and North or South pole (latitude +/- 90.0 deg.).
     */
    public val boundingBox: BoundingBox? = null,

    /**
     * Limit results to one or more countries.
     */
    public val countries: List<IsoCountryCode>? = null,

    /**
     * Filter results to include only a subset (one or more) of the available feature types.
     * See the
     * [Administrative unit types section](https://docs.mapbox.com/api/search/search-box/#administrative-unit-types)
     * for details about the types.
     */
    public val types: List<QueryType>? = null,

    /**
     * Navigation options used for Estimate Time Arrival (ETA) calculation in the response.
     * When this parameters is specified, either [origin] or [proximity] is also should be provided.
     *
     * Enabling ETA calculations will introduce additional latency and incur extra costs,
     * as each search result for which ETAs are calculated (matrix elements) will be billed
     * according to the [Mapbox Matrix API](https://docs.mapbox.com/api/navigation/matrix/) pricing.
     */
    public val navigationOptions: SearchNavigationOptions? = null,

    /**
     * When ETA calculation is enabled, location from which to calculate distance.
     * When both [proximity] and [origin] are provided, [origin] is interpreted
     * as the target of a route, while [proximity] indicates the current user location.
     */
    public val origin: Point? = null,

    /**
     * Request debounce value in milliseconds. Previous request will be cancelled if the new one
     * made within specified by [requestDebounce] time interval.
     */
    public val requestDebounce: Int? = null,

    /**
     * Non-verified query parameters, that will be added to the server API request.
     *
     * Note: Incorrect usage of this parameter may cause failed or malformed response. Do not use it without SDK developers agreement.
     */
    public val unsafeParameters: Map<String, String>? = null,

    /**
     * Specify whether to ignore [com.mapbox.search.record.IndexableRecord] results or not, default is false.
     * When search by [com.mapbox.search.record.IndexableRecord] is enabled, the results can be matched only by
     * query string, while other parameters like [countries], [language], [unsafeParameters]
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
     * Request additional metadata attributes besides the basic ones.
     */
    public val attributeSets: List<AttributeSet>? = null,
) : Parcelable {

    init {
        require(limit == null || limit > 0) { "'limit' should be greater than 0 (passed value: $limit)." }
        require(
            indexableRecordsDistanceThresholdMeters == null || indexableRecordsDistanceThresholdMeters.compareTo(
                0.0
            ) >= 0
        ) {
            "'indexableRecordsDistanceThresholdMeters' can't be negative (passed value: $indexableRecordsDistanceThresholdMeters)"
        }
    }

    /**
     * Creates new [ForwardSearchOptions] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        proximity: Point? = this.proximity,
        boundingBox: BoundingBox? = this.boundingBox,
        countries: List<IsoCountryCode>? = this.countries,
        language: IsoLanguageCode? = this.language,
        limit: Int? = this.limit,
        types: List<QueryType>? = this.types,
        requestDebounce: Int? = this.requestDebounce,
        origin: Point? = this.origin,
        navigationOptions: SearchNavigationOptions? = this.navigationOptions,
        unsafeParameters: Map<String, String>? = this.unsafeParameters,
        ignoreIndexableRecords: Boolean = this.ignoreIndexableRecords,
        indexableRecordsDistanceThresholdMeters: Double? = this.indexableRecordsDistanceThresholdMeters,
        attributeSets: List<AttributeSet>? = this.attributeSets,
    ): ForwardSearchOptions {
        return ForwardSearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            language = language,
            limit = limit,
            types = types,
            requestDebounce = requestDebounce,
            origin = origin,
            navigationOptions = navigationOptions,
            unsafeParameters = unsafeParameters,
            ignoreIndexableRecords = ignoreIndexableRecords,
            indexableRecordsDistanceThresholdMeters = indexableRecordsDistanceThresholdMeters,
            attributeSets = attributeSets,
        )
    }

    /**
     * Creates new [ForwardSearchOptions.Builder] from current [ForwardSearchOptions] instance.
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

        other as ForwardSearchOptions

        if (language != other.language) return false
        if (limit != other.limit) return false
        if (proximity != other.proximity) return false
        if (boundingBox != other.boundingBox) return false
        if (countries != other.countries) return false
        if (types != other.types) return false
        if (navigationOptions != other.navigationOptions) return false
        if (origin != other.origin) return false
        if (requestDebounce != other.requestDebounce) return false
        if (unsafeParameters != other.unsafeParameters) return false
        if (ignoreIndexableRecords != other.ignoreIndexableRecords) return false
        if (!indexableRecordsDistanceThresholdMeters.safeCompareTo(other.indexableRecordsDistanceThresholdMeters)) return false
        if (attributeSets != other.attributeSets) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = language?.hashCode() ?: 0
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (proximity?.hashCode() ?: 0)
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (countries?.hashCode() ?: 0)
        result = 31 * result + (types?.hashCode() ?: 0)
        result = 31 * result + (navigationOptions?.hashCode() ?: 0)
        result = 31 * result + (origin?.hashCode() ?: 0)
        result = 31 * result + (requestDebounce ?: 0)
        result = 31 * result + (unsafeParameters?.hashCode() ?: 0)
        result = 31 * result + ignoreIndexableRecords.hashCode()
        result = 31 * result + (indexableRecordsDistanceThresholdMeters?.hashCode() ?: 0)
        result = 31 * result + (attributeSets?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ForwardSearchOptions(" +
                "language=$language, " +
                "limit=$limit, " +
                "proximity=$proximity, " +
                "boundingBox=$boundingBox, " +
                "countries=$countries, " +
                "types=$types, " +
                "navigationOptions=$navigationOptions, " +
                "origin=$origin, " +
                "requestDebounce=$requestDebounce, " +
                "unsafeParameters=$unsafeParameters, " +
                "ignoreIndexableRecords=$ignoreIndexableRecords, " +
                "indexableRecordsDistanceThresholdMeters=$indexableRecordsDistanceThresholdMeters, " +
                "attributeSets=$attributeSets" +
                ")"
    }

    /**
     * Builder for comfortable creation of [ForwardSearchOptions] instance.
     */
    @Suppress("TooManyFunctions")
    public class Builder() {

        private var proximity: Point? = null
        private var boundingBox: BoundingBox? = null
        private var countries: List<IsoCountryCode>? = null
        private var language: IsoLanguageCode? = defaultLocaleLanguage()
        private var limit: Int? = null
        private var types: List<QueryType>? = null
        private var requestDebounce: Int? = null
        private var origin: Point? = null
        private var navigationOptions: SearchNavigationOptions? = null
        private var unsafeParameters: Map<String, String>? = null
        private var ignoreIndexableRecords: Boolean = false
        private var indexableRecordsDistanceThresholdMeters: Double? = null
        private var attributeSets: List<AttributeSet>? = null

        internal constructor(options: ForwardSearchOptions) : this() {
            proximity = options.proximity
            boundingBox = options.boundingBox
            countries = options.countries
            language = options.language
            limit = options.limit
            types = options.types
            requestDebounce = options.requestDebounce
            origin = options.origin
            navigationOptions = options.navigationOptions
            unsafeParameters = options.unsafeParameters
            ignoreIndexableRecords = options.ignoreIndexableRecords
            indexableRecordsDistanceThresholdMeters =
                options.indexableRecordsDistanceThresholdMeters
            attributeSets = options.attributeSets
        }

        /**
         * Specify the user’s language. This parameter controls the language of the text
         * supplied in responses, and also affects result scoring, with results matching
         * the user’s query in the requested language being preferred over results that
         * match in another language. For example, an autocomplete query for things that start with
         * Frank might return Frankfurt as the first result with an English (en) language parameter,
         * but Frankreich (“France”) with a German (de) language parameter.
         * If language is not set explicitly, then language from default system locale will be used.
         */
        public fun language(language: IsoLanguageCode): Builder = apply { this.language = language }

        /**
         * Specify the maximum number of results to return,
         * including results from [com.mapbox.search.record.IndexableDataProvider].
         * The maximum number of search results returned from the server is 10.
         */
        public fun limit(limit: Int): Builder = apply { this.limit = limit }

        /**
         * Bias the response to favor results that are closer to a specific location.
         * If not provided, the SDK will try to get current location from the
         * [SearchEngineSettings.locationProvider]. If location is not available,
         * the server we use IP as a proximity.
         *
         * When both [proximity] and [origin] are provided, [origin] is interpreted as the target
         * of a route, while proximity indicates the current user location.
         */
        public fun proximity(proximity: Point?): Builder = apply { this.proximity = proximity }

        /**
         * Limit results to only those contained within the supplied bounding box.
         * The bounding box cannot cross the 180th meridian (longitude +/-180.0 deg.)
         * and North or South pole (latitude +/- 90.0 deg.).
         */
        public fun boundingBox(boundingBox: BoundingBox): Builder =
            apply { this.boundingBox = boundingBox }

        /**
         * Limit results to one or more countries.
         */
        public fun countries(vararg countries: IsoCountryCode): Builder =
            apply { this.countries = countries.toList() }

        /**
         * Limit results to one or more countries.
         */
        public fun countries(countries: List<IsoCountryCode>): Builder =
            apply { this.countries = countries }

        /**
         * Filter results to include only a subset (one or more) of the available feature types.
         * See the
         * [Administrative unit types section](https://docs.mapbox.com/api/search/search-box/#administrative-unit-types)
         * for details about the types.
         */
        public fun types(vararg types: QueryType): Builder = apply { this.types = types.toList() }

        /**
         * Filter results to include only a subset (one or more) of the available feature types.
         * See the
         * [Administrative unit types section](https://docs.mapbox.com/api/search/search-box/#administrative-unit-types)
         * for details about the types.
         */
        public fun types(types: List<QueryType>): Builder = apply { this.types = types }

        /**
         * Navigation options used for Estimate Time Arrival (ETA) calculation in the response.
         * When this parameters is specified, either [origin] or [proximity] is also should be provided.
         *
         * Enabling ETA calculations will introduce additional latency and incur extra costs,
         * as each search result for which ETAs are calculated (matrix elements) will be billed
         * according to the [Mapbox Matrix API](https://docs.mapbox.com/api/navigation/matrix/) pricing.
         */
        public fun navigationOptions(navigationOptions: SearchNavigationOptions): Builder = apply {
            this.navigationOptions = navigationOptions
        }

        /**
         * When ETA calculation is enabled, location from which to calculate distance.
         * When both [proximity] and [origin] are provided, [origin] is interpreted
         * as the target of a route, while [proximity] indicates the current user location.
         */
        public fun origin(origin: Point): Builder = apply { this.origin = origin }

        /**
         * Request debounce value in milliseconds. Previous request will be cancelled if the new one
         * made within specified by [requestDebounce] time interval.
         */
        public fun requestDebounce(debounce: Int): Builder =
            apply { this.requestDebounce = debounce }

        /**
         * Non-verified query parameters, that will be added to the server API request.
         *
         * Note: Incorrect usage of this parameter may cause failed or malformed response. Do not use it without SDK developers agreement.
         */
        public fun unsafeParameters(unsafeParameters: Map<String, String>): Builder = apply {
            this.unsafeParameters = unsafeParameters
        }

        /**
         * Specify whether to ignore [com.mapbox.search.record.IndexableRecord] results or not, default is false.
         * When search by [com.mapbox.search.record.IndexableRecord] is enabled, the results can be matched only by
         * query string, while other parameters like [countries], [language], [unsafeParameters]
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
         * Request additional metadata attributes besides the basic ones.
         */
        public fun attributeSets(attributeSets: List<AttributeSet>?): Builder = apply {
            this.attributeSets = attributeSets
        }

        /**
         * Create [ForwardSearchOptions] instance from builder data.
         */
        public fun build(): ForwardSearchOptions = ForwardSearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            language = language,
            limit = limit,
            types = types,
            requestDebounce = requestDebounce,
            origin = origin,
            navigationOptions = navigationOptions,
            unsafeParameters = unsafeParameters,
            ignoreIndexableRecords = ignoreIndexableRecords,
            indexableRecordsDistanceThresholdMeters = indexableRecordsDistanceThresholdMeters,
            attributeSets = attributeSets,
        )
    }
}

@JvmSynthetic
internal fun ForwardSearchOptions.mapToCore(): CoreSearchOptions = createCoreSearchOptions(
    proximity = proximity,
    origin = origin,
    navProfile = navigationOptions?.navigationProfile?.rawName,
    etaType = navigationOptions?.etaType?.rawName,
    bbox = boundingBox?.mapToCore(),
    countries = countries?.map { it.code },
    language = language?.code?.let { listOf(it) },
    limit = limit,
    types = types?.mapToCoreTypes(),
    ignoreUR = ignoreIndexableRecords,
    urDistanceThreshold = indexableRecordsDistanceThresholdMeters,
    requestDebounce = requestDebounce,
    addonAPI = unsafeParameters?.let { (it as? HashMap) ?: HashMap(it) },
    attributeSets = attributeSets?.map { it.mapToCore() },
)
