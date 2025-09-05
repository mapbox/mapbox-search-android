package com.mapbox.search.record

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
}
