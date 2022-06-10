package com.mapbox.search.ui.view.search.address

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchOptions
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.doOnWindowFocus
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.extenstion.showKeyboard
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.favorite.FavoriteTemplate
import com.mapbox.search.ui.view.search.SearchInputView
import kotlinx.parcelize.Parcelize

internal class AddressSearchView : ConstraintLayout {

    var onCloseClickListener: (() -> Unit)? = null
    var searchResultListener: ((SearchResult) -> Unit)? = null
    var onFeedbackClickListener: ((ResponseInfo) -> Unit)? = null

    private val searchInputView: SearchInputView
    private val searchResultsView: SearchResultsView

    var searchMode: SearchMode
        get() = searchResultsView.searchMode
        set(value) {
            searchResultsView.searchMode = value
        }

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

        View.inflate(context, R.layout.mapbox_search_sdk_address_search_view, this)

        id = R.id.address_search_view
        isClickable = true
        isFocusable = true

        searchInputView = findViewById(R.id.search_input_view)
        searchResultsView = findViewById(R.id.search_results_view)

        searchInputView.searchInputCallback = object : SearchInputView.SearchInputCallback {
            override fun onFocusChange(hasFocus: Boolean) {
                if (hasFocus) {
                    doOnWindowFocus {
                        searchInputView.editText.showKeyboard()
                    }
                } else {
                    searchInputView.editText.hideKeyboard()
                }
            }

            override fun onTextFieldClick() {
            }

            override fun onQuery(query: String) {
                searchResultsView.search(query)
            }
        }

        searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {

            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                // do nothing
            }

            override fun onCategoryResult(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                // do nothing
            }

            override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
                searchResultListener?.invoke(searchResult)
            }

            override fun onOfflineSearchResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                // do nothing
            }

            override fun onError(e: Exception) {
                // do nothing
            }

            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
                searchInputView.setQuery(historyRecord.name)
            }

            override fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
                searchInputView.setQuery(suggestion.name)
            }

            override fun onFeedbackClicked(responseInfo: ResponseInfo) {
                onFeedbackClickListener?.invoke(responseInfo)
            }
        })

        findViewById<View>(R.id.close_button).setOnClickListener {
            onCloseClickListener?.invoke()
        }

        searchInputView.requestTextFocus()
        setUpTouchHanding()
    }

    fun initialize(mode: Mode, configuration: SearchBottomSheetView.Configuration) {
        searchResultsView.initialize(configuration.commonSearchViewConfiguration)
        searchResultsView.defaultSearchOptions = mode.searchOptions

        searchInputView.setHint(
            when (mode) {
                is Mode.AddFavorite -> R.string.mapbox_search_sdk_address_search_hint_add_favorite
                is Mode.EditLocation -> R.string.mapbox_search_sdk_address_search_hint_edit_favorite
            }
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpTouchHanding() {
        val listener: (View, MotionEvent) -> Boolean = { _, _ ->
            hideKeyboard()
            false
        }
        searchResultsView.setOnTouchListener(listener)
    }

    sealed class Mode : Parcelable {

        abstract val searchOptions: SearchOptions

        @Parcelize
        data class AddFavorite(
            override val searchOptions: SearchOptions
        ) : Mode()

        sealed class EditLocation : Mode() {

            @Parcelize
            data class ForFavorite(
                val favorite: FavoriteRecord,
                override val searchOptions: SearchOptions
            ) : EditLocation()

            @Parcelize
            data class ForTemplate(
                val template: FavoriteTemplate,
                override val searchOptions: SearchOptions
            ) : EditLocation()
        }
    }
}
