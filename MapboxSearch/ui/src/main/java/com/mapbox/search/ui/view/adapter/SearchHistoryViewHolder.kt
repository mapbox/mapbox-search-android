package com.mapbox.search.ui.view.adapter

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mapbox.search.ui.R
import com.mapbox.search.ui.adapter.engines.SearchEntityPresentation
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.utils.extenstion.setTextAndHideIfBlank
import com.mapbox.search.ui.view.SearchResultAdapterItem

internal class SearchHistoryViewHolder(
    parent: ViewGroup,
    private val listener: SearchViewResultsAdapter.Listener
) : BaseViewHolder<SearchResultAdapterItem.History>(parent, R.layout.mapbox_search_sdk_history_item_layout) {

    private val icon: ImageView = findViewById(R.id.icon)
    private val name: TextView = findViewById(R.id.history_name)
    private val address: TextView = findViewById(R.id.history_address)
    private val searchEntityPresentation = SearchEntityPresentation(context)

    override fun bind(item: SearchResultAdapterItem.History) {
        val drawableRes = if (item.isFavorite) {
            R.drawable.mapbox_search_sdk_ic_favorite_uncategorized
        } else {
            R.drawable.mapbox_search_sdk_ic_history
        }

        icon.setImageResource(drawableRes)
        name.text = item.record.name
        address.setTextAndHideIfBlank(
            searchEntityPresentation.getAddressOrResultType(item.record)
        )

        itemView.setOnClickListener {
            listener.onHistoryItemClick(item)
        }
    }
}

internal class RecentSearchesHeaderViewHolder(parent: ViewGroup) : BaseViewHolder<SearchResultAdapterItem.RecentSearchesHeader>(
    parent, R.layout.mapbox_search_sdk_history_header_layout
) {
    override fun bind(item: SearchResultAdapterItem.RecentSearchesHeader) {
        // Nothing to bind
    }
}

internal class EmptyHistoryViewHolder(parent: ViewGroup) : BaseViewHolder<SearchResultAdapterItem.EmptyHistory>(
    parent, R.layout.mapbox_search_sdk_history_empty_layout
) {
    override fun bind(item: SearchResultAdapterItem.EmptyHistory) {
        // Nothing to bind
    }
}
