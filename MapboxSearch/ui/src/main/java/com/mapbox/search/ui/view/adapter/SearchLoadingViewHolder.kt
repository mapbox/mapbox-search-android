package com.mapbox.search.ui.view.adapter

import android.view.ViewGroup
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.view.SearchResultAdapterItem

internal class SearchLoadingViewHolder(parent: ViewGroup) : BaseViewHolder<SearchResultAdapterItem.Loading>(
    parent, R.layout.mapbox_search_sdk_loading_item_layout
) {
    override fun bind(item: SearchResultAdapterItem.Loading) {
        // Nothing to bind
    }
}
