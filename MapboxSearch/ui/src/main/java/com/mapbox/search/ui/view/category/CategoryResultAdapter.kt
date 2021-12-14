package com.mapbox.search.ui.view.category

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.search.ResponseInfo
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.SearchEntityPresentation
import com.mapbox.search.ui.utils.adapter.BaseRecyclerViewAdapter
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.utils.extenstion.setTextAndHideIfBlank
import com.mapbox.search.ui.utils.format.DistanceFormatter
import com.mapbox.search.ui.view.DistanceUnitType

internal sealed class CategoryResultItem {

    data class Result(
        val searchResult: SearchResult,
        val responseInfo: ResponseInfo,
        val distanceMeters: Double?,
    ) : CategoryResultItem()

    object NoResults : CategoryResultItem()
}

internal class CategoryResultItemViewHolder(
    parent: ViewGroup,
    private val distanceUnitType: DistanceUnitType,
    private val clickListener: (CategoryResultItem.Result) -> Unit
) : BaseViewHolder<CategoryResultItem.Result>(parent, R.layout.mapbox_search_sdk_category_result_item_layout) {

    private val distanceFormatter = DistanceFormatter(context)

    private val nameText = findViewById<TextView>(R.id.search_result_name)
    private val addressText = findViewById<TextView>(R.id.search_result_address)
    private val distanceText = findViewById<TextView>(R.id.search_result_distance)

    override fun bind(item: CategoryResultItem.Result) {
        itemView.setOnClickListener {
            clickListener(item)
        }

        val searchResult = item.searchResult

        nameText.text = searchResult.name
        addressText.text = SearchEntityPresentation.getAddressOrResultType(context, searchResult)

        distanceText.setTextAndHideIfBlank(item.distanceMeters?.let { distance ->
            distanceFormatter.format(distance, distanceUnitType)
        })
    }
}

internal class NoCategorySearchResultsViewHolder(parent: ViewGroup) : BaseViewHolder<CategoryResultItem.NoResults>(
    parent, R.layout.mapbox_search_sdk_result_empty_layout
) {

    init {
        (itemView as TextView).setText(R.string.mapbox_search_sdk_categories_card_no_results)
    }

    override fun bind(item: CategoryResultItem.NoResults) {
        // Nothing to bind
    }
}

internal class CategoryResultAdapter(
    private val distanceUnitType: DistanceUnitType
) : BaseRecyclerViewAdapter<CategoryResultItem, RecyclerView.ViewHolder>() {

    var onItemClickListener: ((CategoryResultItem.Result) -> Unit)? = null

    private val innerOnItemClickListener: (CategoryResultItem.Result) -> Unit = {
        onItemClickListener?.invoke(it)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RESULT -> CategoryResultItemViewHolder(parent, distanceUnitType, innerOnItemClickListener)
            VIEW_TYPE_NO_RESULTS -> NoCategorySearchResultsViewHolder(parent)
            else -> throw IllegalStateException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CategoryResultItem.Result -> {
                holder as CategoryResultItemViewHolder
                holder.bind(item)
            }
            is CategoryResultItem.NoResults -> {
                holder as NoCategorySearchResultsViewHolder
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CategoryResultItem.Result -> VIEW_TYPE_RESULT
            is CategoryResultItem.NoResults -> VIEW_TYPE_NO_RESULTS
        }
    }

    private companion object {
        const val VIEW_TYPE_RESULT = 0
        const val VIEW_TYPE_NO_RESULTS = 1
    }
}
