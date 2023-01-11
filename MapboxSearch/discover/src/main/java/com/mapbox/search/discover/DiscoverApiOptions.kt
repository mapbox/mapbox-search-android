package com.mapbox.search.discover

import android.os.Parcelable
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.common.IsoLanguageCode
import kotlinx.parcelize.Parcelize

/**
 * Options, used for the Discover API requests.
 * @see DiscoverApi
 */
@Parcelize
public class DiscoverApiOptions(

    /**
     * Maximum number of results to return.
     */
    public val limit: Int = 10,

    /**
     * User’s language. This parameter controls the language of the text supplied in responses,
     * and also affects result scoring, with results matching the user’s query in the requested language
     * being preferred over results that match in another language.
     *
     * For example, an autocomplete query for things that start with Frank might return Frankfurt
     * as the first result with an English (en) language parameter,
     * but Frankreich (“France”) with a German (de) language parameter.
     *
     * If language is not set explicitly, then language from default system locale will be used.
     */
    public val language: IsoLanguageCode = defaultLocaleLanguage(),
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiscoverApiOptions

        if (limit != other.limit) return false
        if (language != other.language) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = limit
        result = 31 * result + language.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "DiscoverApiOptions(limit=$limit, language=$language)"
    }
}
