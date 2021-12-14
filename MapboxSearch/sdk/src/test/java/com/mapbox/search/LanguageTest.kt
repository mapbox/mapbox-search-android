package com.mapbox.search

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestFactory

internal class LanguageTest {

    @TestFactory
    fun `Check languages and language code`() = TestCase {
        mapOf(
            Language.ALBANIAN to "sq",
            Language.ARABIC to "ar",
            Language.BOSNIAN to "bs",
            Language.BULGARIAN to "bg",
            Language.CATALAN to "ca",
            Language.CHINESE to "zh",
            Language.CHINESE_SIMPLIFIED to "zh-Hans",
            Language.CHINESE_TRADITIONAL to "zh-Hant",
            Language.CZECH to "cs",
            Language.DANISH to "da",
            Language.DUTCH to "nl",
            Language.ENGLISH to "en",
            Language.FINNISH to "fi",
            Language.FRENCH to "fr",
            Language.GEORGIAN to "ka",
            Language.GERMAN to "de",
            Language.HEBREW to "he",
            Language.HUNGARIAN to "hu",
            Language.ICELANDIC to "is",
            Language.INDONESIAN to "id",
            Language.ITALIAN to "it",
            Language.JAPANESE to "ja",
            Language.KAZAKH to "kk",
            Language.KOREAN to "ko",
            Language.LATVIAN to "lv",
            Language.MONGOLIAN to "mn",
            Language.NORWEGIAN_BOKMAL to "nb",
            Language.POLISH to "pl",
            Language.PORTUGUESE to "pt",
            Language.ROMANIAN to "ro",
            Language.SERBIAN to "sr",
            Language.SLOVAK to "sk",
            Language.SLOVENIAN to "sl",
            Language.SPANISH to "es",
            Language.SWEDISH to "sv",
            Language.TAGALOG to "tl",
            Language.THAI to "th",
            Language.TURKISH to "tr"
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
