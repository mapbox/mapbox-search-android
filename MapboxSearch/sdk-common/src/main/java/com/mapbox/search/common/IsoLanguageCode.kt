package com.mapbox.search.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Iso Language Code.
 * @property code language code in ISO 639-1.
 */
@Parcelize
public class IsoLanguageCode(public val code: String) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IsoLanguageCode

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
        public val ALBANIAN: IsoLanguageCode = IsoLanguageCode("sq")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ARABIC: IsoLanguageCode = IsoLanguageCode("ar")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val BOSNIAN: IsoLanguageCode = IsoLanguageCode("bs")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val BULGARIAN: IsoLanguageCode = IsoLanguageCode("bg")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CATALAN: IsoLanguageCode = IsoLanguageCode("ca")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE: IsoLanguageCode = IsoLanguageCode("zh")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE_SIMPLIFIED: IsoLanguageCode = IsoLanguageCode("zh-Hans")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CHINESE_TRADITIONAL: IsoLanguageCode = IsoLanguageCode("zh-Hant")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val CZECH: IsoLanguageCode = IsoLanguageCode("cs")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val DANISH: IsoLanguageCode = IsoLanguageCode("da")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val DUTCH: IsoLanguageCode = IsoLanguageCode("nl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ENGLISH: IsoLanguageCode = IsoLanguageCode("en")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val FINNISH: IsoLanguageCode = IsoLanguageCode("fi")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val FRENCH: IsoLanguageCode = IsoLanguageCode("fr")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val GEORGIAN: IsoLanguageCode = IsoLanguageCode("ka")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val GERMAN: IsoLanguageCode = IsoLanguageCode("de")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val HEBREW: IsoLanguageCode = IsoLanguageCode("he")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val HUNGARIAN: IsoLanguageCode = IsoLanguageCode("hu")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ICELANDIC: IsoLanguageCode = IsoLanguageCode("is")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val INDONESIAN: IsoLanguageCode = IsoLanguageCode("id")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ITALIAN: IsoLanguageCode = IsoLanguageCode("it")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val JAPANESE: IsoLanguageCode = IsoLanguageCode("ja")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val KAZAKH: IsoLanguageCode = IsoLanguageCode("kk")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val KOREAN: IsoLanguageCode = IsoLanguageCode("ko")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val LATVIAN: IsoLanguageCode = IsoLanguageCode("lv")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val MONGOLIAN: IsoLanguageCode = IsoLanguageCode("mn")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val NORWEGIAN_BOKMAL: IsoLanguageCode = IsoLanguageCode("nb")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val POLISH: IsoLanguageCode = IsoLanguageCode("pl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val PORTUGUESE: IsoLanguageCode = IsoLanguageCode("pt")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val ROMANIAN: IsoLanguageCode = IsoLanguageCode("ro")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SERBIAN: IsoLanguageCode = IsoLanguageCode("sr")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SLOVAK: IsoLanguageCode = IsoLanguageCode("sk")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SLOVENIAN: IsoLanguageCode = IsoLanguageCode("sl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SPANISH: IsoLanguageCode = IsoLanguageCode("es")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val SWEDISH: IsoLanguageCode = IsoLanguageCode("sv")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val TAGALOG: IsoLanguageCode = IsoLanguageCode("tl")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val THAI: IsoLanguageCode = IsoLanguageCode("th")

        /**
         * Predefined language constant.
         */
        @JvmField
        public val TURKISH: IsoLanguageCode = IsoLanguageCode("tr")
    }
}
