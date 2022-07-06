package com.mapbox.search.ui.view.search

import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.common.UiError

internal sealed class SearchResultAdapterItem {

    data class Error(val uiError: UiError) : SearchResultAdapterItem()

    object Loading : SearchResultAdapterItem()

    object EmptyHistory : SearchResultAdapterItem()

    object RecentSearchesHeader : SearchResultAdapterItem()

    data class History(val record: HistoryRecord, val isFavorite: Boolean) : SearchResultAdapterItem()

    object EmptySearchResults : SearchResultAdapterItem()

    data class MissingResultFeedback(val responseInfo: ResponseInfo) : SearchResultAdapterItem()

    sealed class Result : SearchResultAdapterItem() {

        data class Suggestion(
            val suggestion: SearchSuggestion,
            val responseInfo: ResponseInfo,
        ) : Result()

        data class Resolved(
            val resolved: SearchResult,
            val responseInfo: ResponseInfo,
            val distanceMeters: Double?,
            val searchContext: SearchContext,
        ) : Result()
    }
}
