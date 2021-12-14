package com.mapbox.search

import com.mapbox.search.SearchSdkSettings.Companion.DEFAULT_MAX_HISTORY_RECORDS_AMOUNT
import com.mapbox.search.SearchSdkSettings.Companion.MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class SearchSdkSettingsTest {

    @TestFactory
    fun `Check SearchSdkSettings default builder`() = TestCase {
        Given("SearchSdkSettings builder") {
            When("Build new SearchSdkSettings with default values") {
                val actualSettings = SearchSdkSettings.Builder().build()
                val expectedSettings = SearchSdkSettings(
                    geocodingEndpointBaseUrl = "https://api.mapbox.com",
                    singleBoxSearchBaseUrl = null,
                    maxHistoryRecordsAmount = 100
                )

                Then("Settings should be equal", expectedSettings, actualSettings)
            }
        }
    }

    @TestFactory
    fun `Check SearchSdkSettings builder with all values set`() = TestCase {
        Given("SearchSdkSettings builder") {
            When("Build new SearchSdkSettings with all values set") {
                val actualSettings = SearchSdkSettings.Builder()
                    .geocodingEndpointBaseUrl(TEST_GEOCODING_ENDPOINT)
                    .singleBoxSearchBaseUrl(TEST_SBS_ENDPOINT)
                    .maxHistoryRecordsAmount(TEST_MAX_HISTORY_RECORDS_AMOUNT)
                    .build()

                val expectedSettings = SearchSdkSettings(
                    geocodingEndpointBaseUrl = TEST_GEOCODING_ENDPOINT,
                    singleBoxSearchBaseUrl = TEST_SBS_ENDPOINT,
                    maxHistoryRecordsAmount = TEST_MAX_HISTORY_RECORDS_AMOUNT,
                )

                Then("Settings should be equal", expectedSettings, actualSettings)
            }
        }
    }

    @TestFactory
    fun `Check filled SearchSdkSettings toBuilder() function`() = TestCase {
        Given("SearchSdkSettings") {
            When("Use toBuilder() function and then build new SearchSdkSettings") {
                val settings = SearchSdkSettings(
                    geocodingEndpointBaseUrl = TEST_GEOCODING_ENDPOINT,
                    singleBoxSearchBaseUrl = TEST_SBS_ENDPOINT,
                    maxHistoryRecordsAmount = TEST_MAX_HISTORY_RECORDS_AMOUNT,
                )

                Then("Settings should be equal", settings, settings.toBuilder().build())
            }
        }
    }

    @TestFactory
    fun `Check maxHistoryRecordsAmount field initialization`() = TestCase {
        setOf(1, DEFAULT_MAX_HISTORY_RECORDS_AMOUNT, MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND).forEach { inputValue ->
            Given("SearchSdkSettings with valid maxHistoryRecordsAmount = $inputValue") {
                When("Create SearchSdkSettings with constructor") {
                    val settings = SearchSdkSettings(maxHistoryRecordsAmount = inputValue)

                    val actualValue = settings.maxHistoryRecordsAmount
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }

                When("Create SearchSdkSettings with builder") {
                    val settings = SearchSdkSettings.Builder()
                        .maxHistoryRecordsAmount(inputValue)
                        .build()

                    val actualValue = settings.maxHistoryRecordsAmount
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }
            }
        }

        listOf(-1, 0, MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND + 1).forEach { inputValue ->
            Given("SearchSdkSettings with ineligible maxHistoryRecordsAmount = $inputValue") {
                WhenThrows("Create SearchSdkSettings with constructor", IllegalArgumentException::class) {
                    SearchSdkSettings(maxHistoryRecordsAmount = inputValue)
                }

                WhenThrows("Create SearchSdkSettings with builder", IllegalArgumentException::class) {
                    SearchSdkSettings.Builder()
                        .maxHistoryRecordsAmount(inputValue)
                        .build()
                }
            }
        }
    }

    private companion object {
        const val TEST_GEOCODING_ENDPOINT: String = "https://test.geo.com"
        const val TEST_SBS_ENDPOINT: String = "https://test.sbs.com"
        const val TEST_MAX_HISTORY_RECORDS_AMOUNT: Int = 17
    }
}
