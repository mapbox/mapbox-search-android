package com.mapbox.search.ui.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mapbox.search.ui.utils.StringToLongIdMapper
import com.mapbox.search.ui.utils.adapter.BaseRecyclerViewAdapter
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultAdapterItem

internal class SearchViewResultsAdapter(
    private val unitType: DistanceUnitType,
) : BaseRecyclerViewAdapter<SearchResultAdapterItem, ViewHolder>() {

    private val stringToLongMapper = StringToLongIdMapper()

    var listener: Listener? = null

    private val innerListener = object : Listener {

        override fun onHistoryItemClick(item: SearchResultAdapterItem.History) {
            listener?.onHistoryItemClick(item)
        }

        override fun onResultItemClick(item: SearchResultAdapterItem.Result) {
            listener?.onResultItemClick(item)
        }

        override fun onPopulateQueryClick(item: SearchResultAdapterItem.Result) {
            listener?.onResultItemClick(item)
        }

        override fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback) {
            listener?.onMissingResultFeedbackClick(item)
        }

        override fun onErrorItemClick(item: SearchResultAdapterItem.Error) {
            listener?.onErrorItemClick(item)
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return stringToLongMapper.getId(items[position].toString())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOADING -> SearchLoadingViewHolder(parent)
            VIEW_TYPE_ERROR -> SearchErrorViewHolder(parent, innerListener)
            VIEW_TYPE_EMPTY_HISTORY -> EmptyHistoryViewHolder(parent)
            VIEW_TYPE_RESENT_SEARCHES_HEADER -> RecentSearchesHeaderViewHolder(parent)
            VIEW_TYPE_HISTORY -> SearchHistoryViewHolder(parent, innerListener)
            VIEW_TYPE_EMPTY_SEARCH_RESULTS -> EmptySearchResultsViewHolder(parent)
            VIEW_TYPE_SEARCH_RESULT -> SearchResultViewHolder(parent, unitType, innerListener)
            VIEW_TYPE_MISSING_RESULT -> MissingResultFeedbackViewHolder(parent, innerListener)
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
        return when (val item = items[position]) {
            is SearchResultAdapterItem.Loading -> VIEW_TYPE_LOADING
            is SearchResultAdapterItem.Error -> VIEW_TYPE_ERROR
            is SearchResultAdapterItem.EmptyHistory -> VIEW_TYPE_EMPTY_HISTORY
            is SearchResultAdapterItem.RecentSearchesHeader -> VIEW_TYPE_RESENT_SEARCHES_HEADER
            is SearchResultAdapterItem.History -> VIEW_TYPE_HISTORY
            is SearchResultAdapterItem.EmptySearchResults -> VIEW_TYPE_EMPTY_SEARCH_RESULTS
            is SearchResultAdapterItem.Result -> VIEW_TYPE_SEARCH_RESULT
            is SearchResultAdapterItem.MissingResultFeedback -> VIEW_TYPE_MISSING_RESULT
            else -> throw IllegalStateException("Unknown view type: $item")
        }
    }

    interface Listener {
        fun onHistoryItemClick(item: SearchResultAdapterItem.History)
        fun onResultItemClick(item: SearchResultAdapterItem.Result)
        fun onPopulateQueryClick(item: SearchResultAdapterItem.Result)
        fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback)
        fun onErrorItemClick(item: SearchResultAdapterItem.Error)
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
