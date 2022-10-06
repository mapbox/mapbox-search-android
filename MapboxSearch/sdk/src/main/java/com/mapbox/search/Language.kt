package com.mapbox.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Preferred languages of the search results.
 * @property code language code in ISO 639-1.
 */
@Parcelize
public class Language(public val code: String) : Parcelable {

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

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ALBANIAN: Language = Language("sq")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ARABIC: Language = Language("ar")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val BOSNIAN: Language = Language("bs")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val BULGARIAN: Language = Language("bg")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CATALAN: Language = Language("ca")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE: Language = Language("zh")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE_SIMPLIFIED: Language = Language("zh-Hans")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE_TRADITIONAL: Language = Language("zh-Hant")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CZECH: Language = Language("cs")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val DANISH: Language = Language("da")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val DUTCH: Language = Language("nl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ENGLISH: Language = Language("en")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val FINNISH: Language = Language("fi")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val FRENCH: Language = Language("fr")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val GEORGIAN: Language = Language("ka")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val GERMAN: Language = Language("de")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val HEBREW: Language = Language("he")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val HUNGARIAN: Language = Language("hu")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ICELANDIC: Language = Language("is")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val INDONESIAN: Language = Language("id")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ITALIAN: Language = Language("it")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val JAPANESE: Language = Language("ja")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val KAZAKH: Language = Language("kk")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val KOREAN: Language = Language("ko")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val LATVIAN: Language = Language("lv")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val MONGOLIAN: Language = Language("mn")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val NORWEGIAN_BOKMAL: Language = Language("nb")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val POLISH: Language = Language("pl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val PORTUGUESE: Language = Language("pt")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ROMANIAN: Language = Language("ro")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SERBIAN: Language = Language("sr")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SLOVAK: Language = Language("sk")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SLOVENIAN: Language = Language("sl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SPANISH: Language = Language("es")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SWEDISH: Language = Language("sv")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val TAGALOG: Language = Language("tl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val THAI: Language = Language("th")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val TURKISH: Language = Language("tr")
    }
}
