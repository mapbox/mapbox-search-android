package com.mapbox.search.common

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class IsoLanguageTest {

    @TestFactory
    fun `Check languages and language code`() = TestCase {
        mapOf(
            IsoLanguage.ALBANIAN to "sq",
            IsoLanguage.ARABIC to "ar",
            IsoLanguage.BOSNIAN to "bs",
            IsoLanguage.BULGARIAN to "bg",
            IsoLanguage.CATALAN to "ca",
            IsoLanguage.CHINESE to "zh",
            IsoLanguage.CHINESE_SIMPLIFIED to "zh-Hans",
            IsoLanguage.CHINESE_TRADITIONAL to "zh-Hant",
            IsoLanguage.CZECH to "cs",
            IsoLanguage.DANISH to "da",
            IsoLanguage.DUTCH to "nl",
            IsoLanguage.ENGLISH to "en",
            IsoLanguage.FINNISH to "fi",
            IsoLanguage.FRENCH to "fr",
            IsoLanguage.GEORGIAN to "ka",
            IsoLanguage.GERMAN to "de",
            IsoLanguage.HEBREW to "he",
            IsoLanguage.HUNGARIAN to "hu",
            IsoLanguage.ICELANDIC to "is",
            IsoLanguage.INDONESIAN to "id",
            IsoLanguage.ITALIAN to "it",
            IsoLanguage.JAPANESE to "ja",
            IsoLanguage.KAZAKH to "kk",
            IsoLanguage.KOREAN to "ko",
            IsoLanguage.LATVIAN to "lv",
            IsoLanguage.MONGOLIAN to "mn",
            IsoLanguage.NORWEGIAN_BOKMAL to "nb",
            IsoLanguage.POLISH to "pl",
            IsoLanguage.PORTUGUESE to "pt",
            IsoLanguage.ROMANIAN to "ro",
            IsoLanguage.SERBIAN to "sr",
            IsoLanguage.SLOVAK to "sk",
            IsoLanguage.SLOVENIAN to "sl",
            IsoLanguage.SPANISH to "es",
            IsoLanguage.SWEDISH to "sv",
            IsoLanguage.TAGALOG to "tl",
            IsoLanguage.THAI to "th",
            IsoLanguage.TURKISH to "tr"
        ).forEach { (inputValue, expectedValue) ->

            Given("Language = $inputValue") {
                When("Get language code") {
                    val actualValue = inputValue.code
                    Then("It should be <$expectedValue>") {
                        Assertions.assertEquals(expectedValue, actualValue)
                    }
                }
            }
        }
    }
}
