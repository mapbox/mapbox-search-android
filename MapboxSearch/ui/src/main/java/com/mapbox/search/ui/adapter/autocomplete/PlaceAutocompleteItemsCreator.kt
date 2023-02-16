package com.mapbox.search.ui.adapter.autocomplete

import android.content.Context
import androidx.annotation.ColorInt
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.base.utils.extension.distanceTo
import com.mapbox.search.base.utils.extension.lastKnownLocation
import com.mapbox.search.common.HighlightsCalculator
import com.mapbox.search.ui.R
import com.mapbox.search.ui.adapter.BaseItemsCreator
import com.mapbox.search.ui.adapter.engines.SearchEntityPresentation
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.view.SearchResultAdapterItem

internal class PlaceAutocompleteItemsCreator(
    private val context: Context,
    private val locationEngine: LocationEngine,
    private val searchEntityPresentation: SearchEntityPresentation = SearchEntityPresentation(context),
    @ColorInt
    private val selectionSpanColor: Int = context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryAccentColor),
    highlightsCalculator: HighlightsCalculator = HighlightsCalculator.INSTANCE
) : BaseItemsCreator(context, selectionSpanColor, highlightsCalculator) {

    suspend fun createForSuggestions(
        suggestions: List<PlaceAutocompleteSuggestion>,
        query: String
    ): List<SearchResultAdapterItem> {
        if (suggestions.isEmpty()) {
            return listOf(SearchResultAdapterItem.EmptySearchResults)
        }

        return suggestions.map { suggestion ->
            val distance = if (suggestion.distanceMeters != null) {
                suggestion.distanceMeters
            } else {
                locationEngine.lastKnownLocation(context).value?.distanceTo(suggestion.coordinate)
            }

            SearchResultAdapterItem.Result(
                title = highlight(suggestion.name, query),
                subtitle = suggestion.formattedAddress,
                distanceMeters = distance,
                drawable = searchEntityPresentation.getDrawable(suggestion),
                isPopulateQueryVisible = true,
                payload = suggestion,
            )
        }
    }
}
