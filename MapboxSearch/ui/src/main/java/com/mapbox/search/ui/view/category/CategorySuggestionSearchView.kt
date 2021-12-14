package com.mapbox.search.ui.view.category

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchResultsView

internal class CategorySuggestionSearchView : ConstraintLayout {

    var onBackClickListener: (() -> Unit)? = null
    var onCloseClickListener: (() -> Unit)? = null
    var onSearchResultClickListener: ((SearchResult, ResponseInfo) -> Unit)? = null
    var onFeedbackClickListener: ((ResponseInfo) -> Unit)? = null

    private val categoryName: TextView
    private val searchResultsView: SearchResultsView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        val context = if (isInEditMode) {
            wrapWithSearchTheme(context)
        } else {
            context
        }

        isClickable = true
        isFocusable = true

        View.inflate(context, R.layout.mapbox_search_sdk_category_suggestion_search_view, this)

        categoryName = findViewById(R.id.category_name)
        searchResultsView = findViewById(R.id.search_results_view)

        searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {
            override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
                onSearchResultClickListener?.invoke(searchResult, responseInfo)
            }

            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
                // shouldn't be called in this context
            }

            override fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
                // shouldn't be called in this context
            }

            override fun onFeedbackClicked(responseInfo: ResponseInfo) {
                onFeedbackClickListener?.invoke(responseInfo)
            }
        })

        findViewById<View>(R.id.back_button).setOnClickListener {
            onBackClickListener?.invoke()
        }

        findViewById<View>(R.id.close_button).setOnClickListener {
            onCloseClickListener?.invoke()
        }

        setUpTouchHanding()
    }

    fun init(searchSuggestion: SearchSuggestion, configuration: SearchBottomSheetView.Configuration) {
        searchResultsView.initialize(configuration.commonSearchViewConfiguration)
        categoryName.text = searchSuggestion.name
        searchResultsView.searchSuggestion(searchSuggestion)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpTouchHanding() {
        val listener: (View, MotionEvent) -> Boolean = { _, _ ->
            hideKeyboard()
            false
        }
        searchResultsView.setOnTouchListener(listener)
    }
}
