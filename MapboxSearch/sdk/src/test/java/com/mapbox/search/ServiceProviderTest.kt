package com.mapbox.search

import com.mapbox.android.core.location.LocationEngine
import com.mapbox.search.analytics.ErrorsReporter
import com.mapbox.search.analytics.InternalAnalyticsService
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryService
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import org.junit.jupiter.api.TestFactory

internal class ServiceProviderTest {

    @TestFactory
    fun `Check ServiceProviderImpl`() = TestCase {
        Given("ServiceProviderImpl with mocked dependencies") {
            val analyticsService: InternalAnalyticsService = mockk()
            val locationEngine: LocationEngine = mockk()
            val historyDataProvider: HistoryService = mockk()
            val favoritesDataProvider: FavoritesDataProvider = mockk()
            val errorsReporter: ErrorsReporter = mockk()

            val serviceProvider = ServiceProviderImpl(
                analyticsSender = analyticsService,
                locationEngine = locationEngine,
                historyDataProvider = historyDataProvider,
                favoritesDataProvider = favoritesDataProvider,
                errorsReporter = errorsReporter,
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

            When("Get internalAnalyticsService") {
                val value = serviceProvider.internalAnalyticsService()
                Then("Returned value is HighlightsCalculatorImpl", analyticsService, value)
            }

            When("Get locationEngine") {
                val value = serviceProvider.locationEngine()
                Then("Returned value is HighlightsCalculatorImpl", locationEngine, value)
            }

            When("Get analyticsService") {
                val value = serviceProvider.analyticsService()
                Then("Returned value is AnalyticsService", analyticsService, value)
            }

            When("Get errorsReporter") {
                val value = serviceProvider.errorsReporter()
                Then("Returned value is ErrorsReporter", errorsReporter, value)
            }
        }
    }
}
