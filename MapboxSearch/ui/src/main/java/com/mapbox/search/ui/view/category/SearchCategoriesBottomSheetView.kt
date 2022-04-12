package com.mapbox.search.ui.view.category

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.search.ApiType
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.common.SearchCommonAsyncOperationTask
import com.mapbox.search.common.extension.lastKnownLocationOrNull
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.OffsetItemDecoration
import com.mapbox.search.ui.utils.SearchBottomSheetBehavior
import com.mapbox.search.ui.utils.extenstion.collapse
import com.mapbox.search.ui.utils.extenstion.distanceTo
import com.mapbox.search.ui.utils.extenstion.getPixelSize
import com.mapbox.search.ui.utils.extenstion.hide
import com.mapbox.search.ui.utils.extenstion.isCollapsed
import com.mapbox.search.ui.utils.extenstion.isExpanded
import com.mapbox.search.ui.utils.extenstion.isHidden
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.GlobalViewPreferences
import com.mapbox.search.ui.view.SearchSdkFrameLayout
import com.mapbox.search.ui.view.common.MapboxSdkUiErrorView
import com.mapbox.search.ui.view.common.UiError
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import kotlinx.parcelize.Parcelize
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * View that implements category search. It notifies developers via listeners when a user clicks on a `SearchResult`, favorite record or wants to initiate a category search.
 *
 * Note that [SearchCategoriesBottomSheetView.initialize] has to be called in order to make this view work properly.
 */
@Suppress("TooManyFunctions")
public class SearchCategoriesBottomSheetView @JvmOverloads constructor(
    outerContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : SearchSdkFrameLayout(wrapWithSearchTheme(outerContext), attrs, defStyleAttr, defStyleRes), CoordinatorLayout.AttachedBehavior {

    // TODO decouple view from model
    private val locationEngine = MapboxSearchSdk.serviceProvider.locationEngine()
    private var asyncItemsCreatorTask: SearchCommonAsyncOperationTask? = null

    private val searchEngine: SearchEngine

    private val searchCallback = object : SearchCallback {
        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            postNewState(ViewState.Results(results, responseInfo))
        }

        override fun onError(e: Exception) {
            val uiError = UiError.fromException(e)
            postNewState(ViewState.Error(e, uiError))
        }
    }

    private var currentState: ViewState? = null

    private val behavior = SearchBottomSheetBehavior<View>().apply {
        isHideable = true
        state = BottomSheetBehavior.STATE_HIDDEN
    }

    /**
     * The current state of the bottom sheet.
     */
    @get:BottomSheetState
    public val state: Int
        get() = fromBottomSheetState(behavior.state)

    private var previousNonHiddenState: Int = BottomSheetBehavior.STATE_COLLAPSED

    private var searchOptions: CategorySearchOptions = GlobalViewPreferences.DEFAULT_CATEGORY_SEARCH_OPTIONS
    private var latestCategory: Category? = null
    private var currentRequest: SearchRequestTask? = null

    private val onBottomSheetStateChangedListeners = CopyOnWriteArrayList<OnBottomSheetStateChangedListener>()
    private val onCloseClickListeners = CopyOnWriteArrayList<OnCloseClickListener>()
    private val onSearchResultClickListeners = CopyOnWriteArrayList<OnSearchResultClickListener>()
    private val categoryLoadingStateListeners = CopyOnWriteArrayList<CategoryLoadingStateListener>()

    private val mainThreadWorker = SearchSdkMainThreadWorker
    private var stateChangeRunnable: Runnable? = null

    private var commonSearchViewConfiguration = CommonSearchViewConfiguration()
    private var isInitialized = false

    private val categoryNameText: TextView
    private val statusText: TextView
    private val categoriesResultRecycler: RecyclerView
    private val topDivider: View
    private val loadingProgress: View
    private val tongueView: View
    private val uiErrorView: MapboxSdkUiErrorView

    private lateinit var categoriesAdapter: CategoryResultAdapter

    init {
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_search_categories_bottom_sheet, this, true)

        searchEngine = MapboxSearchSdk.getSearchEngine()

        behavior.peekHeight = context.getPixelSize(R.dimen.mapbox_search_sdk_categories_card_peek_height)

        categoryNameText = findViewById(R.id.search_category_name)
        statusText = findViewById(R.id.search_category_status)
        categoriesResultRecycler = findViewById(R.id.categories_result_recycler)
        topDivider = findViewById(R.id.top_divider)
        loadingProgress = findViewById(R.id.categories_loading_progress)
        tongueView = findViewById(R.id.tongue_view)
        uiErrorView = findViewById(R.id.no_connection_view)

        categoriesResultRecycler.layoutManager = LinearLayoutManager(context)
        categoriesResultRecycler.addItemDecoration(
            OffsetItemDecoration(
                context,
                applyPredicate = { viewHolder ->
                    viewHolder is CategoryResultItemViewHolder
                }
            )
        )

        uiErrorView.onRetryClickListener = {
            retryLoadCategories(delayStateChange = true)
        }

        findViewById<View>(R.id.card_close_button).setOnClickListener {
            onCloseClickListeners.forEach { it.onCloseClick() }
        }

        behavior.addOnStateChangedListener { newState, fromUser ->
            if (behavior.isCollapsed || behavior.isExpanded) {
                previousNonHiddenState = newState
            }
            @BottomSheetState
            val bottomSheetState = fromBottomSheetState(newState)
            onBottomSheetStateChangedListeners.forEach { it.onStateChanged(bottomSheetState, fromUser) }
        }
    }

    /**
     * Initializes the inner state of this view and defines configuration options.
     * It's obligatory to call this method as soon as the view is created.
     *
     * @param commonSearchViewConfiguration Configuration options.
     *
     * @throws [IllegalStateException] if this method has already been called.
     */
    public fun initialize(commonSearchViewConfiguration: CommonSearchViewConfiguration) {
        check(!isInitialized) {
            "Already initialized"
        }
        isInitialized = true
        this.commonSearchViewConfiguration = commonSearchViewConfiguration

        categoriesAdapter = CategoryResultAdapter(commonSearchViewConfiguration.distanceUnitType).apply {
            onItemClickListener = { item ->
                onSearchResultClickListeners.forEach { it.onSearchResultClick(item.searchResult, item.responseInfo) }
            }
        }

        categoriesResultRecycler.adapter = categoriesAdapter
    }

    private fun checkInitialized() {
        check(isInitialized) {
            "Initialize this view first"
        }
    }

    /**
     * Switch [SearchCategoriesBottomSheetView] to opened state.
     *
     * @param category [Category] to search.
     * @param searchOptions [CategorySearchOptions] that will be used for search request.
     * @throws [IllegalStateException] if [SearchCategoriesBottomSheetView.initialize] has not been called.
     * @see hide
     * @see restorePreviousNonHiddenState
     */
    @JvmOverloads
    public fun open(category: Category, searchOptions: CategorySearchOptions = this.searchOptions) {
        checkInitialized()
        setCategory(category, searchOptions)
        behavior.collapse()
    }

    /**
     * Restores previous view state for [SearchCategoriesBottomSheetView] with correct [category].
     *
     * @param category [Category] to search.
     * @param searchOptions [CategorySearchOptions] that will be used for search request.
     * @throws [IllegalStateException] if [SearchCategoriesBottomSheetView.initialize] has not been called.
     * @see hide
     * @see open
     */
    @JvmOverloads
    public fun restorePreviousNonHiddenState(
        category: Category,
        searchOptions: CategorySearchOptions = this.searchOptions
    ) {
        checkInitialized()
        setCategory(category, searchOptions)
        behavior.state = previousNonHiddenState
    }

    /**
     * Switch [SearchCategoriesBottomSheetView] to hidden state.
     * @throws [IllegalStateException] if [SearchCategoriesBottomSheetView.initialize] has not been called.
     * @see open
     * @see restorePreviousNonHiddenState
     */
    public fun hide() {
        checkInitialized()
        behavior.hide()
    }

    /**
     * Check if view is hidden in current moment.
     * @return true, if [SearchCategoriesBottomSheetView] is in hidden state.
     * @throws [IllegalStateException] if [SearchCategoriesBottomSheetView.initialize] has not been called.
     */
    public fun isHidden(): Boolean {
        checkInitialized()
        return behavior.isHidden
    }

    private fun setCategory(category: Category, searchOptions: CategorySearchOptions) {
        if (latestCategory == category && currentState !is ViewState.Error) {
            currentState?.takeIf { it is ViewState.Results }?.let { state ->
                state as ViewState.Results
                categoryLoadingStateListeners.forEach {
                    it.onCategoryResultsLoaded(category, state.results, state.responseInfo)
                }
            }
            return
        }
        this.searchOptions = searchOptions
        this.latestCategory = category
        categoryNameText.text = context.getString(category.presentation.displayName)
        loadCategories(category, searchOptions, delayStateChange = false)
    }

    private fun retryLoadCategories(delayStateChange: Boolean = false) {
        latestCategory?.let {
            loadCategories(it, searchOptions, delayStateChange)
        }
    }

    private fun loadCategories(
        category: Category,
        searchOptions:
        CategorySearchOptions,
        delayStateChange: Boolean = false
    ) {
        val canonicalName = when (searchEngine.apiType) {
            ApiType.GEOCODING -> category.geocodingCanonicalName
            ApiType.SBS -> category.sbsCanonicalName
        }
        currentRequest = searchEngine.search(canonicalName, options = searchOptions, callback = searchCallback)
        postNewState(ViewState.Loading, delayed = delayStateChange)
    }

    private fun cancelCategoryLoadingInternal() {
        currentRequest?.cancel()
    }

    /**
     * Cancel current category search.
     * @throws [IllegalStateException] if [SearchCategoriesBottomSheetView.initialize] has not been called.
     */
    public fun cancelCategoryLoading() {
        checkInitialized()
        cancelCategoryLoadingInternal()
        latestCategory = null
    }

    private fun postNewState(state: ViewState, delayed: Boolean = false) {
        stateChangeRunnable?.let {
            mainThreadWorker.cancel(it)
            stateChangeRunnable = null
        }

        if (delayed) {
            Runnable {
                showState(state)
            }.also {
                stateChangeRunnable = it
                mainThreadWorker.postDelayed(delay = STATE_CHANGE_DEBOUNCE_MILLIS, runnable = it)
            }
        } else {
            showState(state)
        }
    }

    private fun showState(state: ViewState) {
        checkInitialized()

        asyncItemsCreatorTask?.cancel()

        currentState = state

        tongueView.isVisible = state is ViewState.Results
        categoryNameText.isInvisible = state is ViewState.Error
        statusText.isVisible = state !is ViewState.Error
        topDivider.isVisible = state !is ViewState.Error
        loadingProgress.isVisible = state is ViewState.Loading
        uiErrorView.isVisible = state is ViewState.Error
        categoriesResultRecycler.isVisible = state is ViewState.Results

        when (state) {
            is ViewState.Results -> {
                createAdapterItems(state.results, state.responseInfo) { items ->
                    latestCategory?.let { category ->
                        categoryLoadingStateListeners.forEach {
                            it.onCategoryResultsLoaded(category, state.results, state.responseInfo)
                        }
                    }
                    categoriesResultRecycler.scrollToPosition(0)
                    categoriesAdapter.items = items
                    statusText.text = context.resources.getQuantityString(
                        R.plurals.mapbox_search_sdk_categories_card_number_of_search_results,
                        state.results.size,
                        state.results.size
                    )
                }
            }
            is ViewState.Loading -> {
                latestCategory?.let { category ->
                    categoryLoadingStateListeners.forEach { it.onLoadingStart(category) }
                }
                categoriesAdapter.clearItems()
                statusText.setText(R.string.mapbox_search_sdk_categories_card_loading)
            }
            is ViewState.Error -> {
                latestCategory?.let { category ->
                    categoryLoadingStateListeners.forEach { it.onLoadingError(category, state.originalError) }
                }
                categoriesAdapter.clearItems()
                statusText.text = null
                uiErrorView.uiError = state.uiError
            }
        }
    }

    private fun createAdapterItems(
        results: List<SearchResult>,
        responseInfo: ResponseInfo,
        callback: (List<CategoryResultItem>) -> Unit,
    ) {
        asyncItemsCreatorTask?.cancel()
        asyncItemsCreatorTask = locationEngine.lastKnownLocationOrNull(context) { location ->
            val items = if (results.isEmpty()) {
                listOf(CategoryResultItem.NoResults)
            } else {
                results.map {
                    val distance = it.coordinate?.let { coordinate ->
                        location?.distanceTo(coordinate)
                    }
                    CategoryResultItem.Result(it, responseInfo, distance)
                }
            }
            callback(items)
        }
    }

    /**
     * Back button handler.
     * @return true if a click has been processed.
     */
    public fun handleOnBackPressed(): Boolean {
        return if (behavior.isExpanded) {
            behavior.collapse()
            true
        } else {
            false
        }
    }

    /**
     * @suppress
     */
    override fun getBehavior(): CoordinatorLayout.Behavior<*> = behavior

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (currentRequest?.isCancelled == true) {
            retryLoadCategories()
        }
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        cancelCategoryLoadingInternal()
        super.onDetachedFromWindow()
    }

    /**
     * @suppress
     */
    override fun onSaveInstanceState(): Parcelable {
        return SavedState(
            cardState = behavior.state,
            peekHeight = behavior.peekHeight,
            previousNonHiddenState = previousNonHiddenState,
            category = latestCategory,
            searchOptions = searchOptions,
            configuration = commonSearchViewConfiguration,
            baseState = super.onSaveInstanceState()
        )
    }

    /**
     * @suppress
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superState)
        savedState?.let {
            behavior.state = it.cardState
            behavior.peekHeight = it.peekHeight
            previousNonHiddenState = it.previousNonHiddenState
            commonSearchViewConfiguration = it.configuration
            it.category?.let { category ->
                setCategory(category, it.searchOptions)
            }
        }
    }

    /**
     * Adds a listener to be notified of bottom sheet events.
     *
     * @param listener The listener to notify when bottom sheet state changes.
     */
    public fun addOnBottomSheetStateChangedListener(listener: OnBottomSheetStateChangedListener) {
        onBottomSheetStateChangedListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnBottomSheetStateChangedListener(listener: OnBottomSheetStateChangedListener) {
        onBottomSheetStateChangedListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of close button click.
     *
     * @param listener The listener to notify when close button clicked.
     */
    public fun addOnCloseClickListener(listener: OnCloseClickListener) {
        onCloseClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnCloseClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnCloseClickListener(listener: OnCloseClickListener) {
        onCloseClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of search result clicks.
     *
     * @param listener The listener to notify when search result clicked.
     */
    public fun addOnSearchResultClickListener(listener: OnSearchResultClickListener) {
        onSearchResultClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnSearchResultClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnSearchResultClickListener(listener: OnSearchResultClickListener) {
        onSearchResultClickListeners.remove(listener)
    }

    /**
     * Adds [CategoryLoadingStateListener] to know exactly when search started, failed or succeeded with some results.
     */
    public fun addCategoryLoadingStateListener(listener: CategoryLoadingStateListener) {
        categoryLoadingStateListeners.add(listener)
    }

    /**
     * Removes a previously added [CategoryLoadingStateListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeCategoryLoadingStateListener(listener: CategoryLoadingStateListener) {
        categoryLoadingStateListeners.remove(listener)
    }

    /**
     * Listener for bottom sheet state changes events.
     */
    public fun interface OnBottomSheetStateChangedListener {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param newState The new state.
         * @param fromUser true if the change event was triggered by a user, for example, by card swipe.
         */
        public fun onStateChanged(@BottomSheetState newState: Int, fromUser: Boolean)
    }

    /**
     * Listener for close button click events.
     */
    public fun interface OnCloseClickListener {

        /**
         * Called when close button clicked.
         */
        public fun onCloseClick()
    }

    /**
     * Listener for search result item click processing.
     * @see addOnSearchResultClickListener
     * @see removeOnSearchResultClickListener
     */
    public fun interface OnSearchResultClickListener {

        /**
         * Called, when search result item clicked.
         */
        public fun onSearchResultClick(searchResult: SearchResult, responseInfo: ResponseInfo)
    }

    /**
     * Listener for category search request state change.
     */
    public interface CategoryLoadingStateListener {

        /**
         * Called when category search request started.
         *
         * @param category [Category] for which loading request was started.
         */
        public fun onLoadingStart(category: Category)

        /**
         * Called when category search request successfully completed.
         *
         * @param category [Category] for which [searchResults] were loaded.
         * @param searchResults Loaded search results for [category].
         * @param responseInfo Search response information.
         */
        public fun onCategoryResultsLoaded(
            category: Category,
            searchResults: List<SearchResult>,
            responseInfo: ResponseInfo
        )

        /**
         * Called when category search request failed.
         *
         * @param category [Category] associated with the failed loading.
         * @param e Exception occurred during loading.
         */
        public fun onLoadingError(category: Category, e: Exception)
    }

    private sealed class ViewState {
        object Loading : ViewState()
        data class Error(val originalError: Exception, val uiError: UiError) : ViewState()
        data class Results(val results: List<SearchResult>, val responseInfo: ResponseInfo) : ViewState()
    }

    @Parcelize
    private class SavedState(
        val cardState: Int,
        val peekHeight: Int,
        val previousNonHiddenState: Int,
        val category: Category?,
        val searchOptions: CategorySearchOptions,
        val configuration: CommonSearchViewConfiguration,
        private val baseState: Parcelable?
    ) : BaseSavedState(baseState)

    /**
     * State of the bottom sheet.
     */
    @IntDef(HIDDEN, COLLAPSED, EXPANDED, DRAGGING, SETTLING)
    @Retention(SOURCE)
    public annotation class BottomSheetState

    /**
     * @suppress
     */
    public companion object {

        /**
         * Hidden state.
         */
        public const val HIDDEN: Int = 1

        /**
         * Collapsed state.
         */
        public const val COLLAPSED: Int = 2

        /**
         * Expanded state.
         */
        public const val EXPANDED: Int = 3

        /**
         * Dragging state.
         */
        public const val DRAGGING: Int = 4

        /**
         * Settling state.
         */
        public const val SETTLING: Int = 5

        @BottomSheetState
        @JvmSynthetic
        internal fun fromBottomSheetState(@BottomSheetBehavior.State state: Int): Int {
            return when (state) {
                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HALF_EXPANDED -> COLLAPSED
                BottomSheetBehavior.STATE_EXPANDED -> EXPANDED
                BottomSheetBehavior.STATE_DRAGGING -> DRAGGING
                BottomSheetBehavior.STATE_SETTLING -> SETTLING
                BottomSheetBehavior.STATE_HIDDEN -> HIDDEN
                else -> throw IllegalStateException("Unprocessed state: $state")
            }
        }

        private const val STATE_CHANGE_DEBOUNCE_MILLIS = 300L
    }
}
