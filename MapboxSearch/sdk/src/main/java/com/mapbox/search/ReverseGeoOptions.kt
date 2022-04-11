package com.mapbox.search

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.core.CoreReverseGeoOptions
import com.mapbox.search.core.CoreReverseMode
import kotlinx.parcelize.Parcelize

/**
 * Decides how results are sorted in a reverse geocoding query if multiple results are requested using a limit other than 1.
 */
public enum class ReverseMode {

    /**
     * Default, causes the closest feature to always be returned first.
     */
    DISTANCE,

    /**
     * Allows high-prominence features to be sorted higher than nearer, lower-prominence features.
     */
    SCORE
}

/**
 * Search options for reverse geocoding.
 * @see SearchEngine
 */
@Parcelize
public class ReverseGeoOptions @JvmOverloads public constructor(

    /**
     * Coordinates to resolve.
     */
    public val center: Point,

    /**
     * Limit results to one or more countries.
     *
     * Note: Supported for Geocoding API only.
     */
    public val countries: List<Country>? = null,

    /**
     * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
     * If language is not set explicitly, then language from default system locale will be used.
     */
    public val languages: List<Language>? = defaultSearchOptionsLanguage(),

    /**
     * Specify the maximum number of results to return. The default is 1 and the maximum supported is 5.
     * The default behavior in reverse geocoding is to return at most one feature at each of the multiple levels of the administrative hierarchy (for example, one address, one region, one country). Increasing the limit allows returning multiple features of the same type, but only for one type (for example, multiple address results). Consequently, setting limit to a higher-than-default value requires specifying exactly one types parameter.
     */
    public val limit: Int? = null,

    /**
     * Decides how results are sorted in a reverse geocoding query.
     *
     * Note: Supported for Geocoding API only.
     */
    public val reverseMode: ReverseMode? = null,

    /**
     * Filter results to include only a subset (one or more) of the available feature types. Options are country, region, postcode, district, place, locality, neighborhood, address, and poi.
     */
    public val types: List<QueryType>? = null,
) : Parcelable {

    init {
        check(limit == null || limit > 0) { "Provided limit should be greater than 0 (was found: $limit)." }
    }

    /**
     * Creates new [ReverseGeoOptions] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        center: Point = this.center,
        countries: List<Country>? = this.countries,
        languages: List<Language>? = this.languages,
        limit: Int? = this.limit,
        reverseMode: ReverseMode? = this.reverseMode,
        types: List<QueryType>? = this.types,
    ): ReverseGeoOptions {
        return ReverseGeoOptions(
            center = center,
            countries = countries,
            languages = languages,
            limit = limit,
            reverseMode = reverseMode,
            types = types,
        )
    }

    /**
     * Creates new [ReverseGeoOptions.Builder] from current [ReverseGeoOptions] instance.
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

        other as ReverseGeoOptions

        if (center != other.center) return false
        if (countries != other.countries) return false
        if (languages != other.languages) return false
        if (limit != other.limit) return false
        if (reverseMode != other.reverseMode) return false
        if (types != other.types) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = center.hashCode()
        result = 31 * result + (countries?.hashCode() ?: 0)
        result = 31 * result + (languages?.hashCode() ?: 0)
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (reverseMode?.hashCode() ?: 0)
        result = 31 * result + (types?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ReverseGeoOptions(" +
                "center=$center, " +
                "countries=$countries, " +
                "languages=$languages, " +
                "limit=$limit, " +
                "reverseMode=$reverseMode, " +
                "types=$types" +
                ")"
    }

    /**
     * Builder for comfortable creation of [ReverseGeoOptions] instance.
     * @property center Coordinates to resolve.
     */
    public class Builder(private val center: Point) {

        private var countries: List<Country>? = null
        private var languages: List<Language>? = defaultSearchOptionsLanguage()
        private var limit: Int? = null
        private var reverseMode: ReverseMode? = null
        private var types: List<QueryType>? = null

        internal constructor(options: ReverseGeoOptions) : this(options.center) {
            countries = options.countries
            languages = options.languages
            limit = options.limit
            reverseMode = options.reverseMode
            types = options.types
        }

        /**
         * Limit results to one or more countries.
         *
         * Note: Supported for Geocoding API only.
         */
        public fun countries(vararg countries: Country): Builder = apply { this.countries = countries.toList() }

        /**
         * Limit results to one or more countries.
         *
         * Note: Supported for Geocoding API only.
         */
        public fun countries(countries: List<Country>): Builder = apply { this.countries = countries }

        /**
         * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
         */
        public fun languages(vararg languages: Language): Builder = apply { this.languages = languages.toList() }

        /**
         * Specify the user’s language. This parameter controls the language of the text supplied in responses, and also affects result scoring, with results matching the user’s query in the requested language being preferred over results that match in another language. For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
         */
        public fun languages(languages: List<Language>): Builder = apply { this.languages = languages }

        /**
         * Specify the maximum number of results to return. The default is 1 and the maximum supported is 5.
         * The default behavior in reverse geocoding is to return at most one feature at each of the multiple levels of the administrative hierarchy (for example, one address, one region, one country). Increasing the limit allows returning multiple features of the same type, but only for one type (for example, multiple address results). Consequently, setting limit to a higher-than-default value requires specifying exactly one types parameter.
         */
        public fun limit(limit: Int): Builder = apply { this.limit = limit }

        /**
         * Decides how results are sorted in a reverse geocoding query.
         *
         * Note: Supported for Geocoding API only.
         */
        public fun reverseMode(reverseMode: ReverseMode): Builder = apply { this.reverseMode = reverseMode }

        /**
         * Filter results to include only a subset (one or more) of the available feature types.
         */
        public fun types(vararg types: QueryType): Builder = apply { this.types = types.toList() }

        /**
         * Filter results to include only a subset (one or more) of the available feature types.
         */
        public fun types(types: List<QueryType>): Builder = apply { this.types = types }

        /**
         * Create [ReverseGeoOptions] instance from builder data.
         */
        public fun build(): ReverseGeoOptions = ReverseGeoOptions(
            center = center,
            countries = countries,
            languages = languages,
            limit = limit,
            reverseMode = reverseMode,
            types = types,
        )
    }
}

@JvmSynthetic
internal fun ReverseMode.mapToCore(): CoreReverseMode {
    return when (this) {
        ReverseMode.DISTANCE -> CoreReverseMode.DISTANCE
        ReverseMode.SCORE -> CoreReverseMode.SCORE
    }
}

@JvmSynthetic
internal fun ReverseGeoOptions.mapToCore(): CoreReverseGeoOptions = CoreReverseGeoOptions(
    center,
    reverseMode?.mapToCore(),
    countries?.map { it.code },
    languages?.map { it.code },
    limit,
    types.mapToCoreTypes()
)
