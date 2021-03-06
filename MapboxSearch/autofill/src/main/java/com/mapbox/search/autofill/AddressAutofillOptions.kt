package com.mapbox.search.autofill

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Options, used for address autofill requests.
 * @see AddressAutofill
 */
@Parcelize
public class AddressAutofillOptions @JvmOverloads public constructor(

    /**
     * Limit results to one or more countries.
     */
    public val countries: List<Country>? = null,

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
    public val language: Language? = defaultSearchOptionsLanguage(),
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressAutofillOptions

        if (countries != other.countries) return false
        if (language != other.language) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = countries?.hashCode() ?: 0
        result = 31 * result + (language?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "AddressAutofillOptions(countries=$countries, language=$language)"
    }

    /**
     * Country code to use in options.
     * @property code country code in ISO 3166 alpha 2.
     */
    @Parcelize
    public class Country(public val code: String) : Parcelable {

        @JvmSynthetic
        internal fun toCoreSdkType() = com.mapbox.search.Country(code)

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Country

            if (code != other.code) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return code.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Country(code='$code')"
        }
    }

    /**
     * Preferred language of the autofill response.
     * @property code language code in ISO 639-1.
     */
    @Parcelize
    public class Language(public val code: String) : Parcelable {

        @JvmSynthetic
        internal fun toCoreSdkType() = com.mapbox.search.Language(code)

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Language

            if (code != other.code) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return code.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Language(code='$code')"
        }
    }

    private companion object {
        fun defaultSearchOptionsLanguage(): Language {
            return Language(Locale.getDefault().language)
        }
    }
}
