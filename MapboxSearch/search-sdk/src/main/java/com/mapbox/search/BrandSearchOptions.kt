@file:OptIn(RestrictedMapboxSearchAPI::class)

package com.mapbox.search

import android.os.Parcelable
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.Reserved.Flags.SEARCH_BOX
import com.mapbox.search.base.core.CoreBrandSearchOptions
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.RestrictedMapboxSearchAPI
import kotlinx.parcelize.Parcelize

/**
 * Search options, used for brand search.
 * @see SearchEngine
 */
@Parcelize
@RestrictedMapboxSearchAPI
@Reserved(SEARCH_BOX)
public class BrandSearchOptions @JvmOverloads public constructor(

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
     * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
     * If language is not set explicitly, then language from default system locale will be used.
     */
    public val language: IsoLanguageCode? = defaultLocaleLanguage(),

    /**
     * Specify the maximum number of results to return.
     * The maximum number of search results is determined by server
     */
    public val limit: Int? = null,

    /**
     * Determines whether to return “closed” POIs.
     */
    public val showClosedPOIs: Boolean? = null,

    /**
     * Non-verified query parameters, that will be added to the server API request.
     *
     * Note: Incorrect usage of this parameter may cause failed or malformed response.
     * Do not use it without SDK developers agreement.
     */
    public val unsafeParameters: Map<String, String>? = null,
) : Parcelable {

    init {
        require(limit == null || limit > 0) { "'limit' should be greater than 0 (passed value: $limit)." }
    }

    /**
     * Creates new [BrandSearchOptions] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        proximity: Point? = this.proximity,
        boundingBox: BoundingBox? = this.boundingBox,
        countries: List<IsoCountryCode>? = this.countries,
        language: IsoLanguageCode? = this.language,
        limit: Int? = this.limit,
        showClosedPOIs: Boolean? = this.showClosedPOIs,
        unsafeParameters: Map<String, String>? = this.unsafeParameters,
    ): BrandSearchOptions {
        return BrandSearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            language = language,
            limit = limit,
            showClosedPOIs = showClosedPOIs,
            unsafeParameters = unsafeParameters,
        )
    }

    /**
     * Creates new [BrandSearchOptions.Builder] from current [BrandSearchOptions] instance.
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

        other as BrandSearchOptions

        if (limit != other.limit) return false
        if (showClosedPOIs != other.showClosedPOIs) return false
        if (proximity != other.proximity) return false
        if (boundingBox != other.boundingBox) return false
        if (countries != other.countries) return false
        if (language != other.language) return false
        if (unsafeParameters != other.unsafeParameters) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = limit ?: 0
        result = 31 * result + (showClosedPOIs?.hashCode() ?: 0)
        result = 31 * result + (proximity?.hashCode() ?: 0)
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (countries?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (unsafeParameters?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "BrandSearchOptions(" +
                "proximity=$proximity, " +
                "boundingBox=$boundingBox, " +
                "countries=$countries, " +
                "language=$language, " +
                "limit=$limit, " +
                "showClosedPOIs=$showClosedPOIs, " +
                "unsafeParameters=$unsafeParameters" +
                ")"
    }

    /**
     * Builder for comfortable creation of [BrandSearchOptions] instance.
     */
    @Suppress("TooManyFunctions")
    public class Builder() {

        private var proximity: Point? = null
        private var boundingBox: BoundingBox? = null
        private var countries: List<IsoCountryCode>? = null
        private var language: IsoLanguageCode? = defaultLocaleLanguage()
        private var limit: Int? = null
        private var showClosedPOIs: Boolean? = null
        private var unsafeParameters: Map<String, String>? = null

        internal constructor(options: BrandSearchOptions) : this() {
            proximity = options.proximity
            boundingBox = options.boundingBox
            countries = options.countries
            language = options.language
            limit = options.limit
            showClosedPOIs = options.showClosedPOIs
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
         * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
         * If language is not set explicitly, then language from default system locale will be used.
         */
        public fun language(language: IsoLanguageCode): Builder = apply { this.language = language }

        /**
         * Specify the maximum number of results to return, including results from [com.mapbox.search.record.IndexableDataProvider].
         * The maximum number of search results is determined by server.
         */
        public fun limit(limit: Int): Builder = apply { this.limit = limit }

        /**
         * Denotes whether to return closed POIs
         */
        public fun showClosedPOIs(showClosedPOIs: Boolean): Builder = apply { this.showClosedPOIs = showClosedPOIs }

        /**
         * Non-verified query parameters, that will be added to the server API request.
         *
         * Note: Incorrect usage of this parameter may cause failed or malformed response.
         * Do not use it without SDK developers agreement.
         */
        public fun unsafeParameters(unsafeParameters: Map<String, String>): Builder = apply {
            this.unsafeParameters = unsafeParameters
        }

        /**
         * Create [BrandSearchOptions] instance from builder data.
         */
        public fun build(): BrandSearchOptions = BrandSearchOptions(
            proximity = proximity,
            boundingBox = boundingBox,
            countries = countries,
            language = language,
            limit = limit,
            showClosedPOIs = showClosedPOIs,
            unsafeParameters = unsafeParameters,
        )
    }
}

@JvmSynthetic
internal fun BrandSearchOptions.mapToCoreBrandOptions(query: String): CoreBrandSearchOptions {
    return CoreBrandSearchOptions(
        query = query,
        language = language?.code?.let {
            listOf(it)
        },
        limit = limit,
        proximity = proximity,
        countries = countries?.map { it.code },
        bbox = boundingBox?.mapToCore(),
        showClosedPois = showClosedPOIs,
        addonAPI = unsafeParameters?.let { (it as? HashMap) ?: HashMap(it) },
    )
}
