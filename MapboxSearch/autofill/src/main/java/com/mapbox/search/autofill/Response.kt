package com.mapbox.search.autofill

import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion

internal sealed class SearchSuggestionsResponse {
    data class Suggestions(
        val suggestions: List<BaseSearchSuggestion>,
        val responseInfo: BaseResponseInfo,
    ) : SearchSuggestionsResponse()

    data class Error(val e: Exception) : SearchSuggestionsResponse()
}

internal sealed class SearchSelectionResponse {

    data class Suggestions(
        val suggestions: List<BaseSearchSuggestion>,
        val responseInfo: BaseResponseInfo,
    ) : SearchSelectionResponse()

    data class Result(
        val suggestion: BaseSearchSuggestion,
        val result: BaseSearchResult,
        val responseInfo: BaseResponseInfo,
    ) : SearchSelectionResponse()

    data class CategoryResult(
        val suggestion: BaseSearchSuggestion,
        val results: List<BaseSearchResult>,
        val responseInfo: BaseResponseInfo,
    ) : SearchSelectionResponse()

    data class Error(val e: Exception) : SearchSelectionResponse()
}

internal sealed class SearchMultipleSelectionResponse {

    data class Results(
        val suggestions: List<BaseSearchSuggestion>,
        val results: List<BaseSearchResult>,
        val responseInfo: BaseResponseInfo,
    ) : SearchMultipleSelectionResponse()

    data class Error(val e: Exception) : SearchMultipleSelectionResponse()
}

internal sealed class SearchResultsResponse {

    data class Results(
        val results: List<BaseSearchResult>,
        val responseInfo: BaseResponseInfo,
    ) : SearchResultsResponse()

    data class Error(val e: Exception) : SearchResultsResponse()
}
