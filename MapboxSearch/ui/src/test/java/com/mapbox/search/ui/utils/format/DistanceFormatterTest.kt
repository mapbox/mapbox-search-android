package com.mapbox.search.ui.utils.format

import android.content.Context
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.TestFactory

internal class DistanceFormatterTest {

    @TestFactory
    fun `Distance formatter test`() = TestCase {
        Given("DistanceFormatter with mocked context") {
            val context = mockContext()
            val distanceFormatter = DistanceFormatter(context)

            When("Format 1.5 km with metric unit system") {
                val formatted = distanceFormatter.format(1500.0, DistanceUnitType.METRIC)
                val expectedDistance = "1.5"
                val expectedValue = formatKm(expectedDistance)
                Then("Formatted distance should be $expectedValue", expectedValue, formatted)
                Verify("Km distance format called") {
                    context.getString(R.string.mapbox_search_sdk_distance_km, expectedDistance)
                }
            }

            When("Format 15 m with metric unit system") {
                val formatted = distanceFormatter.format(15.0, DistanceUnitType.METRIC)
                val expectedDistance = "15"
                val expectedValue = formatMeters(expectedDistance)
                Then("Formatted distance should be $expectedValue", expectedValue, formatted)
                Verify("Meters distance format called") {
                    context.getString(R.string.mapbox_search_sdk_distance_meters, expectedDistance)
                }
            }

            When("Format 1.5 km with imperial unit system") {
                val formatted = distanceFormatter.format(1500.0, DistanceUnitType.IMPERIAL)
                val expectedDistance = "0.9"
                val expectedValue = formatMiles(expectedDistance)
                Then("Formatted distance should be $expectedValue", expectedValue, formatted)
                Verify("Miles distance format called") {
                    context.getString(R.string.mapbox_search_sdk_distance_miles, expectedDistance)
                }
            }

            When("Format 15 m with imperial unit system") {
                val formatted = distanceFormatter.format(15.0, DistanceUnitType.IMPERIAL)
                val expectedDistance = "49"
                val expectedValue = formatFeet(expectedDistance)
                Then("Formatted distance should be $expectedValue", expectedValue, formatted)
                Verify("Feet distance format called") {
                    context.getString(R.string.mapbox_search_sdk_distance_feet, expectedDistance)
                }
            }
        }
    }

    private companion object {

        fun formatMeters(meters: String) = "$meters m"
        fun formatKm(km: String) = "$km km"
        fun formatMiles(miles: String) = "$miles mi"
        fun formatFeet(feet: String) = "$feet ft"

        fun mockContext(): Context {
            return mockk<Context>().apply {
                val metersSlot = slot<String>()
                every { getString(R.string.mapbox_search_sdk_distance_meters, capture(metersSlot)) } answers { formatMeters(metersSlot.captured) }
                val kmSlot = slot<String>()
                every { getString(R.string.mapbox_search_sdk_distance_km, capture(kmSlot)) } answers { formatKm(kmSlot.captured) }
                val milesSlot = slot<String>()
                every { getString(R.string.mapbox_search_sdk_distance_miles, capture(milesSlot)) } answers { formatMiles(milesSlot.captured) }
                val feetSlot = slot<String>()
                every { getString(R.string.mapbox_search_sdk_distance_feet, capture(feetSlot)) } answers { formatFeet(feetSlot.captured) }
            }
        }
    }
}
