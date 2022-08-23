package com.mapbox.search.ui.view.search

import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.search.ResponseInfo
import com.mapbox.search.base.utils.extension.lastKnownLocationOrNull
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.utils.extenstion.distanceTo
import com.mapbox.search.ui.view.common.UiError

internal class SearchResultsItemsCreator(
    private val context: Context,
    private val locationEngine: LocationEngine,
) {

    fun createForHistory(historyItems: List<Pair<HistoryRecord, Boolean>>): List<SearchResultAdapterItem> {
        return if (historyItems.isEmpty()) {
            listOf(SearchResultAdapterItem.EmptyHistory)
        } else {
            ArrayList<SearchResultAdapterItem>(historyItems.size + 1).apply {
                add(SearchResultAdapterItem.RecentSearchesHeader)
                val items = historyItems.map {
                    SearchResultAdapterItem.History(record = it.first, isFavorite = it.second)
                }
                addAll(items)
            }
        }
    }

    fun createForSearchSuggestions(
        suggestions: List<SearchSuggestion>,
        responseInfo: ResponseInfo
    ): List<SearchResultAdapterItem> {
        check(suggestions.isNotEmpty())
        val suggestionItems = suggestions.map { SearchResultAdapterItem.Result.Suggestion(it, responseInfo) }
        return suggestionItems + SearchResultAdapterItem.MissingResultFeedback(responseInfo)
    }

    fun createForSearchResults(
        results: List<SearchResult>,
        responseInfo: ResponseInfo,
        searchContext: SearchContext,
        callback: (List<SearchResultAdapterItem>) -> Unit,
    ): AsyncOperationTask {
        check(results.isNotEmpty())
        return locationEngine.lastKnownLocationOrNull(context) { location ->
            val resultItems = results.map {
                val distance = it.distanceMeters ?: it.coordinate?.let { coordinate ->
                    location?.distanceTo(coordinate)
                }
                SearchResultAdapterItem.Result.Resolved(
                    resolved = it,
                    responseInfo = responseInfo,
                    distanceMeters = distance,
                    searchContext = searchContext,
                )
            }
            callback(resultItems + SearchResultAdapterItem.MissingResultFeedback(responseInfo))
        }
    }

    fun createForEmptySearchResults(responseInfo: ResponseInfo): List<SearchResultAdapterItem> = listOf(
        SearchResultAdapterItem.EmptySearchResults,
        SearchResultAdapterItem.MissingResultFeedback(responseInfo),
    )

    fun createForLoading(): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Loading)

    fun createForError(uiError: UiError): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Error(uiError))
}
