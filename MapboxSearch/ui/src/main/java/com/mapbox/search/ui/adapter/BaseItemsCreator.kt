package com.mapbox.search.ui.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import com.mapbox.search.common.HighlightsCalculator
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.UiError

internal open class BaseItemsCreator(
    private val context: Context,
    @ColorInt
    private val selectionSpanColor: Int = context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryAccentColor),
    private val highlightsCalculator: HighlightsCalculator = HighlightsCalculator.INSTANCE
) {

    open fun highlight(name: String, query: String): CharSequence {
        val highlights = highlightsCalculator.highlights(name, query)

        return SpannableString(name).apply {
            highlights.forEach { (start, end) ->
                setSpan(ForegroundColorSpan(selectionSpanColor), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }

    open fun createForLoading(): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Loading)

    open fun createForError(uiError: UiError): List<SearchResultAdapterItem> = listOf(SearchResultAdapterItem.Error(uiError))
}
