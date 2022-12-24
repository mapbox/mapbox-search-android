package com.mapbox.search.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Preferred languages of the search results.
 * @property code language code in ISO 639-1.
 */
@Parcelize
public class IsoLanguage(public val code: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IsoLanguage

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

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ALBANIAN: IsoLanguage = IsoLanguage("sq")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ARABIC: IsoLanguage = IsoLanguage("ar")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val BOSNIAN: IsoLanguage = IsoLanguage("bs")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val BULGARIAN: IsoLanguage = IsoLanguage("bg")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CATALAN: IsoLanguage = IsoLanguage("ca")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE: IsoLanguage = IsoLanguage("zh")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE_SIMPLIFIED: IsoLanguage = IsoLanguage("zh-Hans")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE_TRADITIONAL: IsoLanguage = IsoLanguage("zh-Hant")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CZECH: IsoLanguage = IsoLanguage("cs")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val DANISH: IsoLanguage = IsoLanguage("da")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val DUTCH: IsoLanguage = IsoLanguage("nl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ENGLISH: IsoLanguage = IsoLanguage("en")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val FINNISH: IsoLanguage = IsoLanguage("fi")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val FRENCH: IsoLanguage = IsoLanguage("fr")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val GEORGIAN: IsoLanguage = IsoLanguage("ka")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val GERMAN: IsoLanguage = IsoLanguage("de")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val HEBREW: IsoLanguage = IsoLanguage("he")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val HUNGARIAN: IsoLanguage = IsoLanguage("hu")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ICELANDIC: IsoLanguage = IsoLanguage("is")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val INDONESIAN: IsoLanguage = IsoLanguage("id")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ITALIAN: IsoLanguage = IsoLanguage("it")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val JAPANESE: IsoLanguage = IsoLanguage("ja")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val KAZAKH: IsoLanguage = IsoLanguage("kk")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val KOREAN: IsoLanguage = IsoLanguage("ko")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val LATVIAN: IsoLanguage = IsoLanguage("lv")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val MONGOLIAN: IsoLanguage = IsoLanguage("mn")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val NORWEGIAN_BOKMAL: IsoLanguage = IsoLanguage("nb")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val POLISH: IsoLanguage = IsoLanguage("pl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val PORTUGUESE: IsoLanguage = IsoLanguage("pt")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ROMANIAN: IsoLanguage = IsoLanguage("ro")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SERBIAN: IsoLanguage = IsoLanguage("sr")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SLOVAK: IsoLanguage = IsoLanguage("sk")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SLOVENIAN: IsoLanguage = IsoLanguage("sl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SPANISH: IsoLanguage = IsoLanguage("es")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SWEDISH: IsoLanguage = IsoLanguage("sv")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val TAGALOG: IsoLanguage = IsoLanguage("tl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val THAI: IsoLanguage = IsoLanguage("th")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val TURKISH: IsoLanguage = IsoLanguage("tr")
    }
}
