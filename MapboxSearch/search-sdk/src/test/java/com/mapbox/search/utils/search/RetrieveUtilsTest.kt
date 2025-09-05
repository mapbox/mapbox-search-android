package com.mapbox.search.utils.search

import com.mapbox.search.ApiType
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows

internal class RetrieveUtilsTest {

    @TestFactory
    fun `Check createSearchResultForRetrieve`() = TestCase {
        Given("a Mapbox ID") {
            val mapboxId = "Random Mapbox ID"

            @Suppress("DEPRECATION")
            When("ApiType is ${ApiType.SBS}") {
                val searchResult = RetrieveUtils.createSearchResultForRetrieve(ApiType.SBS, mapboxId)
                Then("the SearchResult.SuggestAction.endpoint should be retrieve", "retrieve", searchResult.action?.endpoint)
                Then("the SearchResult.SuggestAction.path should be blank", "", searchResult.action?.path)
                Then("the SearchResult.SuggestAction.body should be JSON", """{"id":"$mapboxId"}""", searchResult.action?.body?.toString(Charsets.UTF_8))
                Then("the SearchResult.SuggestAction.path should be null", null, searchResult.action?.query)
                Then("the SearchResult.SuggestAction.path should be false", false, searchResult.action?.multiRetrievable)
            }

            When("ApiType is ${ApiType.GEOCODING}") {
                Then("throw an UnsupportedOperationException") {
                    assertThrows<UnsupportedOperationException> { RetrieveUtils.createSearchResultForRetrieve(ApiType.GEOCODING, mapboxId) }
                }
            }
        }
    }
}
