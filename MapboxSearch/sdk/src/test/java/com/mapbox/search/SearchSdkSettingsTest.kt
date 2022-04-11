package com.mapbox.search

import com.mapbox.search.SearchSdkSettings.Companion.DEFAULT_MAX_HISTORY_RECORDS_AMOUNT
import com.mapbox.search.SearchSdkSettings.Companion.MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class SearchSdkSettingsTest {

    @TestFactory
    fun `Check maxHistoryRecordsAmount field initialization`() = TestCase {
        setOf(1, DEFAULT_MAX_HISTORY_RECORDS_AMOUNT, MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND).forEach { inputValue ->
            Given("SearchSdkSettings with valid maxHistoryRecordsAmount = $inputValue") {
                When("Create SearchSdkSettings with constructor") {
                    val settings = SearchSdkSettings(maxHistoryRecordsAmount = inputValue)

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
            }
        }
    }
}
