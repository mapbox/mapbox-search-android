package com.mapbox.search.ui.view.search

import android.view.ViewGroup
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.view.common.MapboxSdkUiErrorView

internal class SearchErrorViewHolder(parent: ViewGroup, onRetryClickListener: () -> Unit) :
    BaseViewHolder<SearchResultAdapterItem.Error>(
        parent, R.layout.mapbox_search_sdk_error_item_layout
    ) {

    private val uiErrorView: MapboxSdkUiErrorView = itemView as MapboxSdkUiErrorView

    init {
        uiErrorView.onRetryClickListener = onRetryClickListener
    }

    override fun bind(item: SearchResultAdapterItem.Error) {
        uiErrorView.uiError = item.uiError
    }
}
