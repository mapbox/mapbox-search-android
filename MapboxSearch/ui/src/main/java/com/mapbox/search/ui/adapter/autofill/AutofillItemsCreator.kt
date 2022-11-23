package com.mapbox.search.ui.adapter.autofill

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.search.HighlightsCalculator
import com.mapbox.search.ServiceProvider
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.base.utils.extension.lastKnownLocation
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.distanceTo
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.UiError
import kotlinx.coroutines.suspendCancellableCoroutine

internal class AutofillItemsCreator(
    private val context: Context,
    private val locationEngine: LocationEngine,
    @ColorInt
    private val selectionSpanColor: Int = context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryAccentColor),
    private val highlightsCalculator: HighlightsCalculator = ServiceProvider.INSTANCE.highlightsCalculator()
) {

    suspend fun createForSuggestions(
        suggestions: List<AddressAutofillSuggestion>,
        query: String
    ): List<SearchResultAdapterItem> {
        if (suggestions.isEmpty()) {
            return listOf(SearchResultAdapterItem.EmptySearchResults)
        }

        return suggestions.map { suggestion ->
            val locationRequest = locationEngine.lastKnownLocation(context)
            val distance: Double? = locationRequest.value?.distanceTo(suggestion.coordinate)

            SearchResultAdapterItem.Result(
                title = formattedAddress(suggestion.formattedAddress, query),
                subtitle = null,
                distanceMeters = distance,
                drawable = R.drawable.mapbox_search_sdk_ic_search_result_address,
                payload = suggestion
            )
        }
    }

    private fun formattedAddress(name: String, query: String): CharSequence {
        val highlights = highlightsCalculator.highlights(name, query)

        return SpannableString(name).apply {
            highlights.forEach { (start, end) ->
                setSpan(ForegroundColorSpan(selectionSpanColor), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun createForLoading(): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Loading)

    fun createForError(uiError: UiError): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Error(uiError))

    private companion object {

        @SuppressLint("MissingPermission")
        suspend fun LocationEngine.lastKnownLocation(context: Context): Expected<Exception, Point> {
            return suspendCancellableCoroutine { continuation ->
                val task = lastKnownLocation(context) {
                    continuation.resumeWith(Result.success(it))
                }

                continuation.invokeOnCancellation {
                    task.cancel()
                }
            }
        }
    }
}
