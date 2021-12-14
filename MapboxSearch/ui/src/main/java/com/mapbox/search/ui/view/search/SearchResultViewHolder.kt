package com.mapbox.search.ui.view.search

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.SearchEntityPresentation
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setTextAndHideIfBlank
import com.mapbox.search.ui.utils.extenstion.setTintCompat
import com.mapbox.search.ui.utils.format.DistanceFormatter
import com.mapbox.search.ui.utils.maki.MakiToDrawableIdMapper
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.category.Category

internal class SearchResultViewHolder(
    parent: ViewGroup,
    private val unitType: DistanceUnitType,
    private val listener: SearchViewResultsAdapter.SearchListener
) : BaseViewHolder<SearchResultAdapterItem.Result>(parent, R.layout.mapbox_search_sdk_result_item_layout) {

    private val highlightsCalculator = MapboxSearchSdk.serviceProvider.highlightsCalculator()

    private val nameView: TextView = findViewById(R.id.search_result_name)
    private val addressView: TextView = findViewById(R.id.search_result_address)
    private val distanceView: TextView = findViewById(R.id.search_result_distance)
    private val populateView: ImageView = findViewById(R.id.result_populate)
    private val iconView: ImageView = findViewById(R.id.result_icon)

    @ColorInt
    private val selectionStanColor = context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryAccentColor)
    private val distanceFormatter = DistanceFormatter(context)

    override fun bind(item: SearchResultAdapterItem.Result) {
        when (item) {
            is SearchResultAdapterItem.Result.Suggestion -> bindSuggestion(item)
            is SearchResultAdapterItem.Result.Resolved -> bindResult(item)
        }
    }

    private fun bindSuggestion(item: SearchResultAdapterItem.Result.Suggestion) {
        val suggestion = item.suggestion

        nameView.text = formattedResultName(suggestion.name, suggestion.requestOptions.query)
        val description = if (suggestion.type is SearchSuggestionType.Category) {
            // By design we don't have description text for category
            null
        } else {
            SearchEntityPresentation.getAddressOrResultType(context, suggestion)
        }
        addressView.setTextAndHideIfBlank(description)

        distanceView.setTextAndHideIfBlank(suggestion.distanceMeters?.let {
            distanceFormatter.format(it, unitType)
        })

        val categories = listOfNotNull((suggestion.type as? SearchSuggestionType.Category)?.canonicalName)
        val itemDrawable = when (val type = suggestion.type) {
            is SearchSuggestionType.SearchResultSuggestion -> getDrawableForSearchResult(
                maki = suggestion.makiIcon,
                categories = categories,
                types = type.types
            )
            is SearchSuggestionType.Category -> R.drawable.mapbox_search_sdk_ic_mdi_search
            is SearchSuggestionType.IndexableRecordItem -> getDrawableForDataProvider(type)
            is SearchSuggestionType.Query -> pickEntityDrawable(
                makiIcon = suggestion.makiIcon,
                categories = emptyList(),
                fallback = R.drawable.mapbox_search_sdk_ic_mdi_search
            )
            else -> error("Unknown SearchSuggestionType type: $type.")
        }
            .let { context.getDrawableCompat(it) }
            ?.apply {
                val color = when (suggestion.type) {
                    is SearchSuggestionType.Category -> R.attr.mapboxSearchSdkPrimaryAccentColor
                    else -> R.attr.mapboxSearchSdkIconTintColor
                }.let { context.resolveAttrOrThrow(it) }
                setTintCompat(color)
            }

        iconView.setImageDrawable(itemDrawable)

        populateView.isVisible = true
        populateView.setOnClickListener {
            listener.onPopulateQueryClicked(suggestion, item.responseInfo)
        }

        itemView.setOnClickListener {
            listener.onSuggestionItemClicked(suggestion)
        }
    }

    private fun bindResult(item: SearchResultAdapterItem.Result.Resolved) {
        val result = item.resolved
        val responseInfo = item.responseInfo
        val distanceMeters = item.distanceMeters
        val highlightQuery = item.highlightQuery

        nameView.text = if (highlightQuery) {
            formattedResultName(result.name, result.requestOptions.query)
        } else {
            result.name
        }

        val description = SearchEntityPresentation.getAddressOrResultType(context, result)
        addressView.setTextAndHideIfBlank(description)

        distanceView.setTextAndHideIfBlank(distanceMeters?.let {
            distanceFormatter.format(it, unitType)
        })

        val itemDrawable = getDrawableForSearchResult(result.makiIcon, result.categories, result.types)
            .let { context.getDrawableCompat(it) }
            ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkIconTintColor))

        iconView.setImageDrawable(itemDrawable)

        populateView.isVisible = false

        itemView.setOnClickListener {
            listener.onResultItemClicked(result, responseInfo)
        }
    }

    @DrawableRes
    private fun getDrawableForSearchResult(
        maki: String?,
        categories: List<String>,
        types: List<SearchResultType>
    ): Int {
        // We expect to have either combination of ADDRESS, COUNTRY, ..., POSTCODE
        // types or only POI type in list, so only first type is needed.
        return when (types.first()) {
            SearchResultType.ADDRESS,
            SearchResultType.COUNTRY,
            SearchResultType.REGION,
            SearchResultType.PLACE,
            SearchResultType.DISTRICT,
            SearchResultType.LOCALITY,
            SearchResultType.NEIGHBORHOOD,
            SearchResultType.STREET,
            SearchResultType.POSTCODE -> R.drawable.mapbox_search_sdk_ic_search_result_address
            SearchResultType.POI -> pickEntityDrawable(
                maki, categories, R.drawable.mapbox_search_sdk_ic_mdi_search
            )
        }
    }

    @DrawableRes
    private fun getDrawableForDataProvider(indexableRecordItem: SearchSuggestionType.IndexableRecordItem): Int {
        return when {
            indexableRecordItem.isHistoryRecord -> R.drawable.mapbox_search_sdk_ic_history
            indexableRecordItem.isFavoriteRecord -> R.drawable.mapbox_search_sdk_ic_favorite_uncategorized
            else -> R.drawable.mapbox_search_sdk_ic_search_result_place
        }
    }

    @DrawableRes
    private fun pickEntityDrawable(makiIcon: String?, categories: List<String>, @DrawableRes fallback: Int): Int {
        val maki = MakiToDrawableIdMapper.getDrawableIdByMaki(makiIcon)
        return if (maki != null) {
            maki
        } else {
            categories.asSequence()
                .mapNotNull { Category.findByCanonicalName(it)?.presentation }
                .firstOrNull()
                ?.icon ?: fallback
        }
    }

    private fun formattedResultName(name: String, query: String): CharSequence {
        val highlights = highlightsCalculator.highlights(name, query)

        return SpannableString(name).apply {
            highlights.forEach { (start, end) ->
                // Same span object can't be reused for multiple usage,
                // that's why we create a new ForegroundColorSpan on every iteration
                setSpan(ForegroundColorSpan(selectionStanColor), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }
}

internal class EmptySearchResultsViewHolder(parent: ViewGroup) : BaseViewHolder<SearchResultAdapterItem.EmptySearchResults>(
    parent, R.layout.mapbox_search_sdk_result_empty_layout
) {
    override fun bind(item: SearchResultAdapterItem.EmptySearchResults) {
        // Nothing to bind
    }
}

internal class MissingResultFeedbackViewHolder(
    parent: ViewGroup,
    private val listener: SearchViewResultsAdapter.SearchListener
) : BaseViewHolder<SearchResultAdapterItem.MissingResultFeedback>(
    parent, R.layout.mapbox_search_sdk_result_missing_feedback_layout
) {
    override fun bind(item: SearchResultAdapterItem.MissingResultFeedback) {
        itemView.setOnClickListener {
            listener.onFeedbackClicked(item.responseInfo)
        }
    }
}
