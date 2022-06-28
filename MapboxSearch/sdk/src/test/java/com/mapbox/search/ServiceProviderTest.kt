package com.mapbox.search

import com.mapbox.android.core.location.LocationEngine
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryService
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import org.junit.jupiter.api.TestFactory

internal class ServiceProviderTest {

    @TestFactory
    fun `Check ServiceProviderImpl`() = TestCase {
        Given("ServiceProviderImpl with mocked dependencies") {
            val locationEngine: LocationEngine = mockk()
            val historyDataProvider: HistoryService = mockk()
            val favoritesDataProvider: FavoritesDataProvider = mockk()

            val serviceProvider = ServiceProviderImpl(
                locationEngine = locationEngine,
                historyDataProvider = historyDataProvider,
                favoritesDataProvider = favoritesDataProvider,
            )

            When("Get highlightsCalculator") {
                val highlightsCalculator = serviceProvider.highlightsCalculator()
                Then("Returned value is HighlightsCalculatorImpl", true, highlightsCalculator is HighlightsCalculatorImpl)
            }

            When("Get favoritesDataProvider") {
                val value = serviceProvider.favoritesDataProvider()
                Then("Returned value is passed to constructor value", favoritesDataProvider, value)
            }

            When("Get historyDataProvider") {
                val value = serviceProvider.historyDataProvider()
                Then("Returned value is HighlightsCalculatorImpl", historyDataProvider, value)
            }

            When("Get historyService") {
                val value = serviceProvider.historyService()
                Then("Returned value is HighlightsCalculatorImpl", historyDataProvider, value)
            }

            When("Get locationEngine") {
                val value = serviceProvider.locationEngine()
                Then("Returned value is HighlightsCalculatorImpl", locationEngine, value)
            }
        }
    }
}
