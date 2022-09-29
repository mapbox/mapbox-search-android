package com.mapbox.search.ui.view.adapter

import android.view.ViewGroup
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.common.MapboxSdkUiErrorView

internal class SearchErrorViewHolder(
    parent: ViewGroup,
    private val listener: SearchViewResultsAdapter.Listener,
) :
    BaseViewHolder<SearchResultAdapterItem.Error>(
        parent, R.layout.mapbox_search_sdk_error_item_layout
    ) {

    private val uiErrorView: MapboxSdkUiErrorView = itemView as MapboxSdkUiErrorView

    override fun bind(item: SearchResultAdapterItem.Error) {
        uiErrorView.uiError = item.uiError
        uiErrorView.onRetryClickListener = {
            listener.onErrorItemClick(item)
        }
    }
}
