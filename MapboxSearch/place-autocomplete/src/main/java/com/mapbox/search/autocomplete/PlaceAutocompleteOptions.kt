package com.mapbox.search.autocomplete

import android.os.Parcelable
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import kotlinx.parcelize.Parcelize

/**
 * Search options, used for the Place Autocomplete requests.
 * @see PlaceAutocomplete
 */
@Parcelize
public class PlaceAutocompleteOptions @JvmOverloads public constructor(

    /**
     * Maximum number of results to return.
     */
    public val limit: Int = 10,

    /**
     * Limit results to one or more countries.
     */
    public val countries: List<IsoCountryCode>? = null,

    /**
     * Specify the user’s language.
     * This parameter controls the language of the text supplied in responses, and also affects result scoring,
     * with results matching the user’s query in the requested language being preferred over results that match in another language.
     *
     * For example, an autocomplete query for things that start with Frank might return Frankfurt as the first result
     * with an English (en) language parameter, but Frankreich (“France”) with a German (de) language parameter.
     *
     * If language is not set explicitly, then language from default system locale will be used.
     */
    public val language: IsoLanguageCode = defaultLocaleLanguage(),

    /**
     * Limit results to one or more types of features, provided as a comma-separated list.
     * If no types are specified, all possible types may be returned.
     */
    public val types: List<PlaceAutocompleteType>? = null,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAutocompleteOptions

        if (limit != other.limit) return false
        if (countries != other.countries) return false
        if (language != other.language) return false
        if (types != other.types) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = limit
        result = 31 * result + (countries?.hashCode() ?: 0)
        result = 31 * result + language.hashCode()
        result = 31 * result + types.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "PlaceAutocompleteOptions(" +
                "limit=$limit, " +
                "countries=$countries, " +
                "language=$language, " +
                "types=$types" +
                ")"
    }
}
