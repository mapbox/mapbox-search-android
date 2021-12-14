package com.mapbox.search.ui.utils.adapter

import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView

internal abstract class BaseRecyclerViewAdapter<I, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    @get:Keep
    var items: List<I> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = items.size

    fun clearItems() {
        items = emptyList()
    }
}
