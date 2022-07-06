package com.mapbox.search.ui.view.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.utils.StringToLongIdMapper
import com.mapbox.search.ui.utils.adapter.BaseRecyclerViewAdapter
import com.mapbox.search.ui.view.DistanceUnitType

internal class SearchViewResultsAdapter(
    private val unitType: DistanceUnitType,
) : BaseRecyclerViewAdapter<SearchResultAdapterItem, ViewHolder>() {

    private val stringToLongMapper = StringToLongIdMapper()

    var searchResultsListener: SearchListener? = null
    var onRetryClickListener: (() -> Unit)? = null

    private val innerSearchResultsViewCallback = object : SearchListener {
        override fun onSuggestionItemClicked(searchSuggestion: SearchSuggestion) {
            searchResultsListener?.onSuggestionItemClicked(searchSuggestion)
        }

        override fun onResultItemClicked(
            searchContext: SearchContext,
            searchResult: SearchResult,
            responseInfo: ResponseInfo
        ) {
            searchResultsListener?.onResultItemClicked(searchContext, searchResult, responseInfo)
        }

        override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
            searchResultsListener?.onHistoryItemClicked(historyRecord)
        }

        override fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
            searchResultsListener?.onPopulateQueryClicked(suggestion, responseInfo)
        }

        override fun onFeedbackClicked(responseInfo: ResponseInfo) {
            searchResultsListener?.onFeedbackClicked(responseInfo)
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        val stringId = when (val item = items[position]) {
            is SearchResultAdapterItem.Loading -> "Loading"
            is SearchResultAdapterItem.Error -> "Error: ${item.uiError}"
            is SearchResultAdapterItem.EmptyHistory -> "Empty history"
            is SearchResultAdapterItem.RecentSearchesHeader -> "Recent searches"
            is SearchResultAdapterItem.History -> "History entry: ${item.record.id}, isFavorite: ${item.isFavorite}"
            is SearchResultAdapterItem.EmptySearchResults -> "Empty search results"
            is SearchResultAdapterItem.Result -> when (item) {
                is SearchResultAdapterItem.Result.Suggestion ->
                    "Search result: ${item.suggestion.id}, distance: ${item.suggestion.distanceMeters}"
                is SearchResultAdapterItem.Result.Resolved ->
                    "Search result: ${item.resolved.id}, " +
                            "distance: ${item.distanceMeters}, " +
                            "searchContext: ${item.searchContext}"
            }
            is SearchResultAdapterItem.MissingResultFeedback -> "Missing result feedback: ${item.responseInfo}"
        }
        return stringToLongMapper.getId(stringId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOADING -> SearchLoadingViewHolder(parent)
            VIEW_TYPE_ERROR -> SearchErrorViewHolder(parent) {
                onRetryClickListener?.invoke()
            }
            VIEW_TYPE_EMPTY_HISTORY -> EmptyHistoryViewHolder(parent)
            VIEW_TYPE_RESENT_SEARCHES_HEADER -> RecentSearchesHeaderViewHolder(parent)
            VIEW_TYPE_HISTORY -> SearchHistoryViewHolder(parent, innerSearchResultsViewCallback)
            VIEW_TYPE_EMPTY_SEARCH_RESULTS -> EmptySearchResultsViewHolder(parent)
            VIEW_TYPE_SEARCH_RESULT -> SearchResultViewHolder(parent, unitType, innerSearchResultsViewCallback)
            VIEW_TYPE_MISSING_RESULT -> MissingResultFeedbackViewHolder(parent, innerSearchResultsViewCallback)
            else -> throw IllegalStateException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SearchResultAdapterItem.Error -> {
                holder as SearchErrorViewHolder
                holder.bind(item)
            }
            is SearchResultAdapterItem.Loading -> {
                holder as SearchLoadingViewHolder
                holder.bind(item)
            }
            is SearchResultAdapterItem.EmptyHistory -> {
                holder as EmptyHistoryViewHolder
                holder.bind(item)
            }
            is SearchResultAdapterItem.RecentSearchesHeader -> {
                holder as RecentSearchesHeaderViewHolder
                holder.bind(item)
            }
            is SearchResultAdapterItem.History -> {
                holder as SearchHistoryViewHolder
                holder.bind(item)
            }
            is SearchResultAdapterItem.EmptySearchResults -> {
                holder as EmptySearchResultsViewHolder
                holder.bind(item)
            }
            is SearchResultAdapterItem.Result -> {
                holder as SearchResultViewHolder
                holder.bind(item)
            }
            is SearchResultAdapterItem.MissingResultFeedback -> {
                holder as MissingResultFeedbackViewHolder
                holder.bind(item)
            }
            else -> {
                throw IllegalStateException("Unknown item $item")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SearchResultAdapterItem.Loading -> VIEW_TYPE_LOADING
            is SearchResultAdapterItem.Error -> VIEW_TYPE_ERROR
            is SearchResultAdapterItem.EmptyHistory -> VIEW_TYPE_EMPTY_HISTORY
            is SearchResultAdapterItem.RecentSearchesHeader -> VIEW_TYPE_RESENT_SEARCHES_HEADER
            is SearchResultAdapterItem.History -> VIEW_TYPE_HISTORY
            is SearchResultAdapterItem.EmptySearchResults -> VIEW_TYPE_EMPTY_SEARCH_RESULTS
            is SearchResultAdapterItem.Result -> VIEW_TYPE_SEARCH_RESULT
            is SearchResultAdapterItem.MissingResultFeedback -> VIEW_TYPE_MISSING_RESULT
        }
    }

    interface SearchListener {

        fun onSuggestionItemClicked(searchSuggestion: SearchSuggestion)

        fun onResultItemClicked(searchContext: SearchContext, searchResult: SearchResult, responseInfo: ResponseInfo)

        fun onHistoryItemClicked(historyRecord: HistoryRecord)

        fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo)

        fun onFeedbackClicked(responseInfo: ResponseInfo)
    }

    private companion object {
        var NEXT_VIEW_TYPE_ID = 0

        val VIEW_TYPE_LOADING = NEXT_VIEW_TYPE_ID++
        val VIEW_TYPE_ERROR = NEXT_VIEW_TYPE_ID++
        val VIEW_TYPE_EMPTY_HISTORY = NEXT_VIEW_TYPE_ID++
        val VIEW_TYPE_RESENT_SEARCHES_HEADER = NEXT_VIEW_TYPE_ID++
        val VIEW_TYPE_HISTORY = NEXT_VIEW_TYPE_ID++
        val VIEW_TYPE_EMPTY_SEARCH_RESULTS = NEXT_VIEW_TYPE_ID++
        val VIEW_TYPE_SEARCH_RESULT = NEXT_VIEW_TYPE_ID++
        val VIEW_TYPE_MISSING_RESULT = NEXT_VIEW_TYPE_ID++
    }
}
