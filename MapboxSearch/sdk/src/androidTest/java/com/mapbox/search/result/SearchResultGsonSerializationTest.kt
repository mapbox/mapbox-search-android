package com.mapbox.search.result

import com.google.gson.Gson
import com.mapbox.search.tests_support.createTestSearchResult
import com.mapbox.search.tests_support.createTestSuggestion
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SearchResultGsonSerializationTest {

    private val gson = Gson()

    @Test
    fun searchSuggestionTest() {
        testObjectSerialization(createTestSuggestion())
    }

    @Test
    fun searchResultTest() {
        testObjectSerialization(createTestSearchResult())
    }

    private fun testObjectSerialization(obj: Any) {
        val json = gson.toJson(obj)
        // Just check for non-emptiness, and no crashes during serialization
        assertTrue(json.isNotEmpty())
    }
}
