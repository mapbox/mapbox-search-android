package com.mapbox.search.ui.view.adapter

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setTextAndHideIfBlank
import com.mapbox.search.ui.utils.extenstion.setTintCompat
import com.mapbox.search.ui.utils.format.DistanceFormatter
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultAdapterItem

internal class SearchResultViewHolder(
    parent: ViewGroup,
    private val unitType: DistanceUnitType,
    private val listener: SearchViewResultsAdapter.Listener
) : BaseViewHolder<SearchResultAdapterItem.Result>(
    parent, R.layout.mapbox_search_sdk_result_item_layout
) {

    private val constraintLayout: ConstraintLayout = findViewById(R.id.search_result_item)
    private val nameView: TextView = findViewById(R.id.search_result_name)
    private val addressView: TextView = findViewById(R.id.search_result_address)
    private val distanceView: TextView = findViewById(R.id.search_result_distance)
    private val populateView: ImageView = findViewById(R.id.result_populate)
    private val iconView: ImageView = findViewById(R.id.result_icon)

    private val distanceFormatter = DistanceFormatter(context)

    override fun bind(item: SearchResultAdapterItem.Result) {
        nameView.text = item.title
        addressView.setTextAndHideIfBlank(item.subtitle?.toString())
        distanceView.setTextAndHideIfBlank(item.distanceMeters?.let {
            distanceFormatter.format(it, unitType)
        })

        with(ConstraintSet()) {
            clone(constraintLayout)

            if (addressView.isVisible) {
                connect(
                    R.id.search_result_distance,
                    ConstraintSet.BASELINE,
                    R.id.search_result_address,
                    ConstraintSet.BASELINE,
                    0
                )

                connect(
                    R.id.search_result_name,
                    ConstraintSet.END,
                    R.id.result_populate,
                    ConstraintSet.START,
                    0
                )

                nameView.maxLines = 1
            } else {
                connect(
                    R.id.search_result_distance,
                    ConstraintSet.BASELINE,
                    R.id.search_result_name,
                    ConstraintSet.BASELINE,
                    0
                )

                connect(
                    R.id.search_result_name,
                    ConstraintSet.END,
                    R.id.search_result_distance,
                    ConstraintSet.START,
                    0
                )

                nameView.maxLines = 2
            }

            applyTo(constraintLayout)
        }

        val color =
            item.drawableColor ?: context.resolveAttrOrThrow(R.attr.mapboxSearchSdkIconTintColor)
        val itemDrawable = context.getDrawableCompat(item.drawable)?.setTintCompat(color)

        iconView.setImageDrawable(itemDrawable)

        populateView.isVisible = item.isPopulateQueryVisible
        if (item.isPopulateQueryVisible) {
            populateView.setOnClickListener {
                listener.onPopulateQueryClick(item)
            }
        }

        itemView.setOnClickListener {
            listener.onResultItemClick(item)
        }
    }
}

internal class EmptySearchResultsViewHolder(
    parent: ViewGroup
) : BaseViewHolder<SearchResultAdapterItem.EmptySearchResults>(
    parent, R.layout.mapbox_search_sdk_result_empty_layout
) {
    override fun bind(item: SearchResultAdapterItem.EmptySearchResults) {
        // Nothing to bind
    }
}

internal class MissingResultFeedbackViewHolder(
    parent: ViewGroup,
    private val listener: SearchViewResultsAdapter.Listener
) : BaseViewHolder<SearchResultAdapterItem.MissingResultFeedback>(
    parent, R.layout.mapbox_search_sdk_result_missing_feedback_layout
) {
    override fun bind(item: SearchResultAdapterItem.MissingResultFeedback) {
        itemView.setOnClickListener {
            listener.onMissingResultFeedbackClick(item)
        }
    }
}
