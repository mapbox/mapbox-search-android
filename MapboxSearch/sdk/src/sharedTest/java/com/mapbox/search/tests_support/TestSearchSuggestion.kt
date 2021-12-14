package com.mapbox.search.tests_support

import com.mapbox.search.RequestOptions
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import kotlinx.parcelize.Parcelize

@Parcelize
internal class TestSearchSuggestion(
    private val description: String = "Test suggestion",
    override val requestOptions: RequestOptions = createTestRequestOptions("Test query")
) : SearchSuggestion {

    override val id: String
        get() = description

    override val name: String
        get() = description

    override val descriptionText: String?
        get() = description

    override val address: SearchAddress?
        get() = null

    override val distanceMeters: Double?
        get() = null

    override val makiIcon: String?
        get() = null

    override val type: SearchSuggestionType
        get() = SearchSuggestionType.SearchResultSuggestion(SearchResultType.POI)

    override val etaMinutes: Double?
        get() = null

    override val isBatchResolveSupported: Boolean
        get() = false
}
