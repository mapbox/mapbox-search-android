package com.mapbox.search.ui.view.search

import android.view.ViewGroup
import android.widget.TextView
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setCompoundDrawableStartWithIntrinsicBounds
import com.mapbox.search.ui.utils.extenstion.setTintCompat

internal class SearchHistoryViewHolder(
    parent: ViewGroup,
    private val listener: SearchViewResultsAdapter.SearchListener
) : BaseViewHolder<SearchResultAdapterItem.History>(parent, R.layout.mapbox_search_sdk_history_item_layout) {

    private val nameView: TextView = findViewById(R.id.history_item)

    override fun bind(item: SearchResultAdapterItem.History) {
        val drawableRes = if (item.isFavorite) {
            R.drawable.mapbox_search_sdk_ic_favorite_uncategorized
        } else {
            R.drawable.mapbox_search_sdk_ic_history
        }

        val drawable = context
            .getDrawableCompat(drawableRes)
            ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryTextInactiveColor))

        nameView.setCompoundDrawableStartWithIntrinsicBounds(drawable)

        nameView.text = item.record.name
        itemView.setOnClickListener {
            listener.onHistoryItemClicked(item.record)
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
