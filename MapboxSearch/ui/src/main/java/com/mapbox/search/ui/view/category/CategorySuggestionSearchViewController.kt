package com.mapbox.search.ui.view.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.search.ResponseInfo
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.common.BaseSearchController

internal class CategorySuggestionSearchViewController : BaseSearchController {

    var onCategoryResultsShownClickListener: ((SearchSuggestion, List<SearchResult>, ResponseInfo) -> Unit)? = null
    var onSuggestionsShownClickListener: ((List<SearchSuggestion>, ResponseInfo) -> Unit)? = null
    var onSearchResultClickListener: ((SearchResult, ResponseInfo) -> Unit)? = null
    var onErrorShownClickListener: ((Exception) -> Unit)? = null
    var onFeedbackClickListener: ((ResponseInfo) -> Unit)? = null
    var onBackClickListener: (() -> Unit)? = null
    var onCloseSearchClickListener: (() -> Unit)? = null

    private val searchSuggestion: SearchSuggestion
    private val configuration: SearchBottomSheetView.Configuration

    override val cardDraggingAllowed: Boolean = true

    constructor(
        searchSuggestion: SearchSuggestion,
        configuration: SearchBottomSheetView.Configuration
    ) : super(bundleSearchSuggestion(searchSuggestion, configuration)) {
        this.searchSuggestion = searchSuggestion
        this.configuration = configuration
    }

    // Android Studio marks this constructor as unused, but it's needed for Controller
    constructor(bundle: Bundle) : super(bundle) {
        searchSuggestion = requireNotNull(bundle.getParcelable(BUNDLE_KEY_SEARCH_SUGGESTION))
        configuration = requireNotNull(bundle.getParcelable(BUNDLE_KEY_CONFIGURATION))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return CategorySuggestionSearchView(container.context).apply {
            init(searchSuggestion, configuration)
            bindActions(this)
        }
    }

    private fun bindActions(view: CategorySuggestionSearchView) {
        view.onCategoryResultsShownClickListener = { suggestion, results, responseInfo ->
            onCategoryResultsShownClickListener?.invoke(suggestion, results, responseInfo)
        }
        view.onSuggestionsShownClickListener = { suggestions, responseInfo ->
            onSuggestionsShownClickListener?.invoke(suggestions, responseInfo)
        }
        view.onSearchResultClickListener = { searchResult, responseInfo ->
            onSearchResultClickListener?.invoke(searchResult, responseInfo)
        }
        view.onErrorShownClickListener = { e ->
            onErrorShownClickListener?.invoke(e)
        }
        view.onFeedbackClickListener = {
            onFeedbackClickListener?.invoke(it)
        }
        view.onBackClickListener = {
            onBackClickListener?.invoke()
        }
        view.onCloseClickListener = {
            onCloseSearchClickListener?.invoke()
        }
    }

    internal companion object {

        const val TAG = "controller.tag.CategorySuggestionSearchViewController"

        private const val BUNDLE_KEY_SEARCH_SUGGESTION = "key.CategorySearchViewController.SearchSuggestion"
        private const val BUNDLE_KEY_CONFIGURATION = "key.CategorySearchViewController.Configuration"

        private fun bundleSearchSuggestion(
            searchSuggestion: SearchSuggestion,
            configuration: SearchBottomSheetView.Configuration,
        ): Bundle {
            return Bundle().apply {
                putParcelable(BUNDLE_KEY_SEARCH_SUGGESTION, searchSuggestion)
                putParcelable(BUNDLE_KEY_CONFIGURATION, configuration)
            }
        }
    }
}
