package com.mapbox.search

import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import org.junit.jupiter.api.TestFactory

internal class ServiceProviderTest {

    @TestFactory
    fun `Check ServiceProviderImpl`() = TestCase {
        Given("ServiceProviderImpl with mocked dependencies") {
            val historyDataProvider: HistoryDataProviderImpl = mockk()
            val favoritesDataProvider: FavoritesDataProvider = mockk()

            val serviceProvider = ServiceProviderImpl(
                historyDataProviderInitializer = { historyDataProvider },
                favoritesDataProviderInitializer = { favoritesDataProvider },
            )

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
        }
    }
}
