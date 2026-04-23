package com.mapbox.search.record

import com.mapbox.geojson.Point
import com.mapbox.search.result.NewSearchResultType
import com.mapbox.search.tests_support.createSearchAddress
import com.mapbox.search.tests_support.createTestFavoriteRecord
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class FavoriteRecordTest {

    @TestFactory
    fun `Check index tokens`() = TestCase {
        Given("FavoriteRecord") {
            When("FavoriteRecord doesn't have address") {
                val recordWithoutAddress = createTestFavoriteRecord(address = null)
                Then("Index tokens should be empty", emptyList<String>(), recordWithoutAddress.indexTokens)
            }

            When("FavoriteRecord have address with filled fields") {
                val address = createSearchAddress(
                    place = "test place", street = "test street", houseNumber = "test houseNumber"
                )
                Then(
                    "Index tokens should contain place, street, houseNumber",
                    listOf("test place", "test street", "test houseNumber"),
                    createTestFavoriteRecord(address = address).indexTokens
                )
            }
        }
    }

    @TestFactory
    fun `Check timestamp`() = TestCase {
        Given("FavoriteRecord") {
            When("Created without explicit timestamp") {
                val record = FavoriteRecord(
                    id = "id",
                    name = "name",
                    descriptionText = null,
                    address = null,
                    routablePoints = null,
                    categories = null,
                    makiIcon = null,
                    coordinate = Point.fromLngLat(0.0, 0.0),
                    metadata = null,
                    newType = NewSearchResultType.POI,
                )
                Then(
                    "Timestamp defaults to UNKNOWN_TIMESTAMP",
                    FavoriteRecord.UNKNOWN_TIMESTAMP,
                    record.timestamp
                )
            }

            When("Created with explicit timestamp") {
                val ts = 1_700_000_000_000L
                val record = createTestFavoriteRecord(timestamp = ts)
                Then("Timestamp is preserved", ts, record.timestamp)
            }

            When("copy() is called without changing timestamp") {
                val ts = 1_700_000_000_000L
                val record = createTestFavoriteRecord(timestamp = ts)
                val copied = record.copy()
                Then("Timestamp is carried over", ts, copied.timestamp)
            }

            When("copy() is called with a new timestamp") {
                val original = createTestFavoriteRecord(timestamp = 1_000L)
                val newTs = 2_000L
                val copied = original.copy(timestamp = newTs)
                Then("New timestamp is applied", newTs, copied.timestamp)
            }
        }
    }
}
