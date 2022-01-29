package com.mapbox.search.result

import com.mapbox.search.tests_support.createTestSearchResult
import com.mapbox.search.tests_support.createTestSuggestion
import com.mapbox.search.utils.assertEqualsButNotSame
import com.mapbox.search.utils.cloneFromParcel
import org.junit.Test

internal class SearchResultParcelizationTest {

    @Test
    fun searchSuggestionTest() {
        val suggestion = createTestSuggestion()
        val clonedSuggestion = suggestion.cloneFromParcel()
        assertEqualsButNotSame(suggestion, clonedSuggestion)
    }

    @Test
    fun searchResultTest() {
        val result = createTestSearchResult()
        val clonedEvent = result.cloneFromParcel()
        assertEqualsButNotSame(result, clonedEvent)
    }
}
