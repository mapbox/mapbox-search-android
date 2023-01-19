package com.mapbox.search.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class IsoLanguageCodeTest {

    @ParameterizedTest
    @MethodSource("allLanguages")
    fun `Check languages and language code`(entry: Map.Entry<IsoLanguageCode, String>) {
        assertEquals(entry.value, entry.key.code)
    }

    private companion object {

        @JvmStatic
        fun allLanguages(): Set<Map.Entry<IsoLanguageCode, String>> = mapOf(
            IsoLanguageCode.ALBANIAN to "sq",
            IsoLanguageCode.ARABIC to "ar",
            IsoLanguageCode.BOSNIAN to "bs",
            IsoLanguageCode.BULGARIAN to "bg",
            IsoLanguageCode.CATALAN to "ca",
            IsoLanguageCode.CHINESE to "zh",
            IsoLanguageCode.CHINESE_SIMPLIFIED to "zh-Hans",
            IsoLanguageCode.CHINESE_TRADITIONAL to "zh-Hant",
            IsoLanguageCode.CZECH to "cs",
            IsoLanguageCode.DANISH to "da",
            IsoLanguageCode.DUTCH to "nl",
            IsoLanguageCode.ENGLISH to "en",
            IsoLanguageCode.FINNISH to "fi",
            IsoLanguageCode.FRENCH to "fr",
            IsoLanguageCode.GEORGIAN to "ka",
            IsoLanguageCode.GERMAN to "de",
            IsoLanguageCode.HEBREW to "he",
            IsoLanguageCode.HUNGARIAN to "hu",
            IsoLanguageCode.ICELANDIC to "is",
            IsoLanguageCode.INDONESIAN to "id",
            IsoLanguageCode.ITALIAN to "it",
            IsoLanguageCode.JAPANESE to "ja",
            IsoLanguageCode.KAZAKH to "kk",
            IsoLanguageCode.KOREAN to "ko",
            IsoLanguageCode.LATVIAN to "lv",
            IsoLanguageCode.MONGOLIAN to "mn",
            IsoLanguageCode.NORWEGIAN_BOKMAL to "nb",
            IsoLanguageCode.POLISH to "pl",
            IsoLanguageCode.PORTUGUESE to "pt",
            IsoLanguageCode.ROMANIAN to "ro",
            IsoLanguageCode.SERBIAN to "sr",
            IsoLanguageCode.SLOVAK to "sk",
            IsoLanguageCode.SLOVENIAN to "sl",
            IsoLanguageCode.SPANISH to "es",
            IsoLanguageCode.SWEDISH to "sv",
            IsoLanguageCode.TAGALOG to "tl",
            IsoLanguageCode.THAI to "th",
            IsoLanguageCode.TURKISH to "tr"
        ).entries
    }
}
