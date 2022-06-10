package com.mapbox.search.ui.view.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchOptions
import com.mapbox.search.common.failDebug
import com.mapbox.search.common.logger.logd
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.getPixelSize
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.CardStateListener
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchBottomSheetView.BottomSheetState
import com.mapbox.search.ui.view.SearchBottomSheetView.CollapsedStateAnchor
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.CategoryEntry
import com.mapbox.search.ui.view.category.CategoryViewCallback
import com.mapbox.search.ui.view.category.HotCategoryView
import com.mapbox.search.ui.view.favorite.FavoriteActionsCallback
import com.mapbox.search.ui.view.favorite.FavoriteActionsDialogFactory
import com.mapbox.search.ui.view.favorite.FavoriteViewCallback
import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItem
import com.mapbox.search.ui.view.search.SearchInputView
import com.mapbox.search.ui.view.tabviewcontainer.TabViewContainer
import kotlinx.parcelize.Parcelize

internal class MainScreenView : ConstraintLayout {

    private val favoritesDataProvider = MapboxSearchSdk.serviceProvider.favoritesDataProvider()
    private var removeFavoriteTask: AsyncOperationTask? = null

    private lateinit var searchInputView: SearchInputView
    private val tabViewContainer: TabViewContainer

    private val searchResultsGroup: View
    private val searchResultsView: SearchResultsView

    private val hotCategoriesGroup: ViewGroup

    var searchMode: SearchMode
        get() = searchResultsView.searchMode
        set(value) {
            searchResultsView.searchMode = value
        }

    var searchOptions: SearchOptions
        get() = searchResultsView.defaultSearchOptions
        set(value) {
            searchResultsView.defaultSearchOptions = value
        }

    var ignoreNextRestoreInstanceState = false

    var onCategoryClickListener: ((Category) -> Unit)? = null
    var onCategoryResultsShownClickListener: ((SearchSuggestion, List<SearchResult>, ResponseInfo) -> Unit)? = null
    var onSuggestionsShownClickListener: ((List<SearchSuggestion>, ResponseInfo) -> Unit)? = null
    var onSearchResultClickListener: ((SearchResult, ResponseInfo) -> Unit)? = null
    var onErrorShownClickListener: ((Exception) -> Unit)? = null
    var onCategorySuggestionClickListener: ((SearchSuggestion) -> Unit)? = null
    var cardStateListener: CardStateListener? = null
    var onHistoryItemClickListener: ((HistoryRecord) -> Unit)? = null
    var onFavoriteClickListener: ((UserFavoriteAdapterItem.Favorite) -> Unit)? = null
    var onEditLocationClickListener: ((FavoriteRecord) -> Unit)? = null
    var onFavoriteRenameListener: ((FavoriteRecord) -> Unit)? = null
    var onFavoriteAddListener: (() -> Unit)? = null
    var onFeedbackClickListener: ((ResponseInfo) -> Unit)? = null

    private var currentConfiguration: SearchBottomSheetView.Configuration? = null
    private var currentState: ViewState = ViewState.Categories
    private val searchInputViewCallback = object : SearchInputView.SearchInputCallback {

        override fun onFocusChange(hasFocus: Boolean) {
            if (hasFocus) {
                cardStateListener?.onExpandCard()

                if (currentState !is ViewState.Search) {
                    moveToState(ViewState.Search(""))
                }
            } else {
                hideKeyboard()
            }
        }

        override fun onTextFieldClick() {
            cardStateListener?.onExpandCard()
        }

        override fun onQuery(query: String) {
            if (searchInputView.hasTextFocus()) {
                moveToState(ViewState.Search(query))
            }
        }
    }

    private val searchResultsViewListener = object : SearchResultsView.SearchListener {

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            onCategoryResultsShownClickListener?.invoke(suggestion, results, responseInfo)
        }

        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            onSuggestionsShownClickListener?.invoke(suggestions, responseInfo)
        }

        override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
            onSearchResultClickListener?.invoke(searchResult, responseInfo)
        }

        override fun onOfflineSearchResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            // do nothing
        }

        override fun onError(e: Exception) {
            onErrorShownClickListener?.invoke(e)
        }

        override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
            onHistoryItemClickListener?.invoke(historyRecord)
        }

        override fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
            searchInputView.setQuery(suggestion.name)
        }

        override fun onFeedbackClicked(responseInfo: ResponseInfo) {
            onFeedbackClickListener?.invoke(responseInfo)
        }
    }

    private val favoriteViewCallback = object : FavoriteViewCallback {
        override fun onMoreActionsClick(userFavoriteItem: UserFavoriteAdapterItem.Favorite.Created) {
            FavoriteActionsDialogFactory()
                .create(context, favoriteActionsCallback, userFavoriteItem)
                .show()
        }

        override fun onItemClick(userFavoriteItem: UserFavoriteAdapterItem.Favorite) {
            onFavoriteClickListener?.invoke(userFavoriteItem)
        }

        override fun onAddFavoriteClick() {
            onFavoriteAddListener?.invoke()
        }
    }

    private val categoryViewCallback = object : CategoryViewCallback {
        override fun onItemClick(categoryEntry: CategoryEntry) {
            onCategoryClickListener?.invoke(categoryEntry.category)
        }
    }

    private val favoriteActionsCallback = object : FavoriteActionsCallback {

        override fun onItemRenameAction(userFavorite: FavoriteRecord) {
            onFavoriteRenameListener?.invoke(userFavorite)
        }

        override fun onItemDeleteAction(userFavorite: FavoriteRecord) {
            removeFavoriteTask = favoritesDataProvider.remove(userFavorite.id, object : CompletionCallback<Boolean> {
                override fun onComplete(result: Boolean) {
                    logd("User favorite $userFavorite removed: $result")
                }

                override fun onError(e: Exception) {
                    Toast.makeText(context, R.string.mapbox_search_sdk_favorite_delete_error, Toast.LENGTH_SHORT).show()

                    failDebug(e) {
                        "Unable to remove favorite: $userFavorite"
                    }
                }
            })
        }

        override fun onItemEditLocationAction(userFavorite: FavoriteRecord) {
            onEditLocationClickListener?.invoke(userFavorite)
        }
    }

    private val bottomSheetCallback = object : SearchBottomSheetView.OnBottomSheetStateChangedListener {
        override fun onStateChanged(@BottomSheetState newState: Int, fromUser: Boolean) {
            if (newState == SearchBottomSheetView.COLLAPSED) {
                resetViewState()
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val context = if (isInEditMode) {
            wrapWithSearchTheme(context)
        } else {
            context
        }

        View.inflate(context, R.layout.mapbox_search_sdk_screen_main_search, this)

        isClickable = true
        isFocusableInTouchMode = true

        searchInputView = findViewById(R.id.search_input_edit_text)
        searchInputView.searchInputCallback = searchInputViewCallback

        searchResultsGroup = findViewById(R.id.search_results_group)
        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            // In order to avoid overdraw,
            // we reset searchResultsView's background because main screen already provides its background
            background = null
            addSearchListener(searchResultsViewListener)
            addOnSuggestionClickListener { suggestion ->
                if (suggestion.type is SearchSuggestionType.Category) {
                    onCategorySuggestionClickListener?.invoke(suggestion)
                    true
                } else {
                    false
                }
            }
            setHasFixedSize(true)
        }

        tabViewContainer = findViewById(R.id.search_tab_container)
        tabViewContainer.favoriteViewCallback = favoriteViewCallback
        tabViewContainer.categoryViewCallback = categoryViewCallback

        hotCategoriesGroup = findViewById(R.id.hot_categories)

        moveToState(currentState)
        setUpTouchHanding()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpTouchHanding() {
        val listener: (View, MotionEvent) -> Boolean = { _, _ ->
            hideKeyboard()
            false
        }
        searchResultsView.setOnTouchListener(listener)
        tabViewContainer.setOnTouchListener(listener)
    }

    @UiThread
    fun initializeSearch(configuration: SearchBottomSheetView.Configuration) {
        if (currentConfiguration == null) {
            searchResultsView.initialize(configuration.commonSearchViewConfiguration)
        }

        if (currentConfiguration == configuration) {
            return
        }

        if (currentConfiguration?.favoriteTemplates != configuration.favoriteTemplates) {
            tabViewContainer.setFavoriteTemplates(configuration.favoriteTemplates)
        }

        if (currentConfiguration?.hotCategories != configuration.hotCategories) {
            hotCategoriesGroup.removeAllViews()
            configuration.hotCategories.forEach { category ->
                val hotCategoryView = HotCategoryView(context).apply {
                    this.category = category
                    setOnClickListener {
                        this.category?.let {
                            onCategoryClickListener?.invoke(it)
                        }
                    }
                }
                hotCategoriesGroup.addView(hotCategoryView)
            }
            val paddingBottom = if (configuration.hotCategories.isEmpty()) {
                0
            } else {
                context.getPixelSize(R.dimen.mapbox_search_sdk_primary_layout_offset)
            }
            hotCategoriesGroup.updatePadding(bottom = paddingBottom)
        }

        if (currentConfiguration?.collapsedStateAnchor != configuration.collapsedStateAnchor) {
            if (isLaidOut) {
                requestLayout()
            }
        }

        currentConfiguration = configuration
    }

    private fun resetViewState() {
        searchInputView.setQuery("")
        searchInputView.clearTextFocus()
        tabViewContainer.moveToInitialPage()
        moveToState(ViewState.Categories)
    }

    fun requestTextFocus() {
        searchInputView.requestTextFocus()
    }

    fun onAttachedToBottomSheetView(view: SearchBottomSheetView) {
        when (view.state) {
            SearchBottomSheetView.COLLAPSED -> resetViewState()
            SearchBottomSheetView.EXPANDED -> {
                // When user gets back from Category suggestion screen,
                // we want to restore focus for searchInputView.
                if (currentState is ViewState.Search) {
                    requestTextFocus()
                }
            }
        }
        view.addOnBottomSheetStateChangedListener(bottomSheetCallback)
    }

    fun onDetachedFromBottomSheetView(view: SearchBottomSheetView) {
        view.removeOnBottomSheetStateChangedListener(bottomSheetCallback)
    }

    fun cardPeekHeight(): Int {
        val tabViewContainerHeight = tabViewContainer.height
        return when {
            tabViewContainerHeight <= 0 -> -1
            else -> {
                val offset = when (currentConfiguration?.collapsedStateAnchor ?: CollapsedStateAnchor.HOT_CATEGORIES) {
                    CollapsedStateAnchor.HOT_CATEGORIES -> tabViewContainerHeight
                    CollapsedStateAnchor.SEARCH_BAR -> tabViewContainerHeight + hotCategoriesGroup.height
                }
                this.height - offset
            }
        }
    }

    private fun moveToState(state: ViewState, fromRestore: Boolean = false) {
        currentState = state

        val isInitialScreen = state is ViewState.Categories
        hotCategoriesGroup.isVisible = isInitialScreen
        setTabsVisibility(isInitialScreen)

        searchResultsGroup.isVisible = state is ViewState.Search
        if (state is ViewState.Search) {
            searchResultsView.search(state.query)
        }

        if (fromRestore && !isInitialScreen) {
            post {
                searchInputView.requestTextFocus()
            }
        }
    }

    private fun setTabsVisibility(visible: Boolean) {
        tabViewContainer.isVisible = visible
        tabViewContainer.excludeViewPager(exclude = !visible)
    }

    override fun onSaveInstanceState(): Parcelable {
        ignoreNextRestoreInstanceState = false
        return SavedState(currentState, super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superState)
        if (ignoreNextRestoreInstanceState) {
            ignoreNextRestoreInstanceState = false
            return
        }
        savedState?.let {
            moveToState(it.viewState, fromRestore = true)
        }
    }

    override fun onDetachedFromWindow() {
        removeFavoriteTask?.cancel()
        removeFavoriteTask = null
        super.onDetachedFromWindow()
    }

    private sealed class ViewState : Parcelable {
        @Parcelize
        object Categories : ViewState()

        @Parcelize
        data class Search(val query: String) : ViewState()
    }

    @Parcelize
    private class SavedState(val viewState: ViewState, private val baseState: Parcelable?) : BaseSavedState(baseState)
}
