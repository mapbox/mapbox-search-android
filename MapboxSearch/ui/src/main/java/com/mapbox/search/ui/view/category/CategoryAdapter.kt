package com.mapbox.search.ui.view.category

import android.view.ViewGroup
import android.widget.TextView
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.adapter.BaseRecyclerViewAdapter
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.utils.extenstion.drawableStart
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setCompoundDrawableStartWithIntrinsicBounds
import com.mapbox.search.ui.utils.extenstion.setTintCompat

internal class CategoryAdapter : BaseRecyclerViewAdapter<CategoryEntry, BaseViewHolder<CategoryEntry>>() {

    var categoryViewCallback: CategoryViewCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<CategoryEntry> {
        return CategoryItemViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<CategoryEntry>, position: Int) {
        holder.bind(items[position])
    }

    private inner class CategoryItemViewHolder(parent: ViewGroup) : BaseViewHolder<CategoryEntry>(
        parent, R.layout.mapbox_search_sdk_category_item_layout
    ) {

        private val nameView: TextView = findViewById(R.id.category_item)

        override fun bind(item: CategoryEntry) {
            nameView.text = context.getString(item.category.presentation.displayName)
            nameView.setCompoundDrawableStartWithIntrinsicBounds(item.category.presentation.icon)

            nameView.drawableStart = nameView.drawableStart
                ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkIconTintColor))

            itemView.setOnClickListener {
                categoryViewCallback?.onItemClick(item)
            }
        }
    }
}
