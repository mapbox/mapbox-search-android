package com.mapbox.search.ui.adapter.engines

import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.search.ResponseInfo
import com.mapbox.search.base.utils.extension.distanceTo
import com.mapbox.search.base.utils.extension.lastKnownLocationOrNull
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.UiError

internal class SearchResultsItemsCreator(
    private val context: Context,
    private val searchEntityPresentation: SearchEntityPresentation = SearchEntityPresentation(context),
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
        if (suggestions.isEmpty()) {
            return createForEmptySearchResults(responseInfo)
        }
        val suggestionItems = suggestions.map { suggestion ->
            SearchResultAdapterItem.Result(
                title = searchEntityPresentation.getTitle(suggestion),
                subtitle = searchEntityPresentation.getDescription(suggestion),
                distanceMeters = suggestion.distanceMeters,
                drawable = searchEntityPresentation.getDrawable(suggestion),
                drawableColor = when (suggestion.type) {
                    is SearchSuggestionType.Category -> R.attr.mapboxSearchSdkPrimaryAccentColor
                    else -> R.attr.mapboxSearchSdkIconTintColor
                }.let { context.resolveAttrOrThrow(it) },
                isPopulateQueryVisible = true,
                payload = suggestion to responseInfo
            )
        }
        return suggestionItems + SearchResultAdapterItem.MissingResultFeedback(responseInfo)
    }

    fun createForSearchResults(
        results: List<SearchResult>,
        responseInfo: ResponseInfo,
        callback: (List<SearchResultAdapterItem>) -> Unit,
    ): AsyncOperationTask {
        if (results.isEmpty()) {
            callback(createForEmptySearchResults(responseInfo))
            return AsyncOperationTask.COMPLETED
        }
        return locationEngine.lastKnownLocationOrNull(context) { location ->
            val resultItems = results.map { result ->
                val distance = result.distanceMeters ?: location?.distanceTo(result.coordinate)

                SearchResultAdapterItem.Result(
                    title = result.name,
                    subtitle = searchEntityPresentation.getDescription(result),
                    distanceMeters = distance,
                    drawable = searchEntityPresentation.getDrawableForSearchResult(result),
                    payload = result to responseInfo
                )
            }
            callback(resultItems + SearchResultAdapterItem.MissingResultFeedback(responseInfo))
        }
    }

    fun createForOfflineSearchResults(
        results: List<OfflineSearchResult>,
        responseInfo: OfflineResponseInfo,
        callback: (List<SearchResultAdapterItem>) -> Unit,
    ): AsyncOperationTask {
        if (results.isEmpty()) {
            callback(
                listOf(
                    SearchResultAdapterItem.EmptySearchResults,
                )
            )
            return AsyncOperationTask.COMPLETED
        }
        return locationEngine.lastKnownLocationOrNull(context) { location ->
            val resultItems = results.map { searchResult ->
                val distance = searchResult.distanceMeters ?: location?.distanceTo(searchResult.coordinate)
                SearchResultAdapterItem.Result(
                    title = searchEntityPresentation.getTitle(searchResult, responseInfo.requestOptions.query),
                    subtitle = searchEntityPresentation.getDescription(searchResult),
                    distanceMeters = distance,
                    drawable = R.drawable.mapbox_search_sdk_ic_search_result_address,
                    payload = searchResult to responseInfo
                )
            }
            callback(resultItems)
        }
    }

    private fun createForEmptySearchResults(responseInfo: ResponseInfo): List<SearchResultAdapterItem> = listOf(
        SearchResultAdapterItem.EmptySearchResults,
        SearchResultAdapterItem.MissingResultFeedback(responseInfo),
    )

    fun createForLoading(): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Loading)

    fun createForError(uiError: UiError): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Error(uiError))
}
