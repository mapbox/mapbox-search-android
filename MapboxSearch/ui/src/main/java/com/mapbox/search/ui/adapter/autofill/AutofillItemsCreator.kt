package com.mapbox.search.ui.adapter.autofill

import android.content.Context
import androidx.annotation.ColorInt
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationProvider
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.base.utils.extension.distanceTo
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.common.HighlightsCalculator
import com.mapbox.search.ui.R
import com.mapbox.search.ui.adapter.BaseItemsCreator
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.view.SearchResultAdapterItem
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class AutofillItemsCreator(
    private val context: Context,
    private val locationProvider: LocationProvider,
    @ColorInt
    private val selectionSpanColor: Int = context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryAccentColor),
    highlightsCalculator: HighlightsCalculator = HighlightsCalculator.INSTANCE
) : BaseItemsCreator(context, selectionSpanColor, highlightsCalculator) {

    suspend fun createForSuggestions(
        suggestions: List<AddressAutofillSuggestion>,
        query: String
    ): List<SearchResultAdapterItem> {
        if (suggestions.isEmpty()) {
            return listOf(SearchResultAdapterItem.EmptySearchResults)
        }

        return suggestions.map { suggestion ->
            val locationRequest = suspendCoroutine { continuation: Continuation<Location?> ->
                locationProvider.getLastLocation { continuation.resume(it) }
            }?.toPoint()
            val distance: Double? = locationRequest?.distanceTo(suggestion.coordinate)

            SearchResultAdapterItem.Result(
                title = highlight(suggestion.formattedAddress, query),
                subtitle = null,
                distanceMeters = distance,
                drawable = R.drawable.mapbox_search_sdk_ic_search_result_address,
                payload = suggestion
            )
        }
    }
}
