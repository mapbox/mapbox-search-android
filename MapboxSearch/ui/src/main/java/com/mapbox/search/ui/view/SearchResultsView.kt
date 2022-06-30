package com.mapbox.search.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.UiThread
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.mapbox.common.ReachabilityFactory
import com.mapbox.common.ReachabilityInterface
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.OfflineSearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.common.SearchCommonAsyncOperationTask
import com.mapbox.search.common.concurrent.checkMainThread
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.throwDebug
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.utils.HistoryRecordsInteractor
import com.mapbox.search.ui.utils.OffsetItemDecoration
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.common.UiError
import com.mapbox.search.ui.view.search.SearchHistoryViewHolder
import com.mapbox.search.ui.view.search.SearchResultAdapterItem
import com.mapbox.search.ui.view.search.SearchResultViewHolder
import com.mapbox.search.ui.view.search.SearchResultsItemsCreator
import com.mapbox.search.ui.view.search.SearchViewResultsAdapter
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Simplified search view.
 *
 * Note that [SearchResultsView.initialize] has to be called in order to make this view work properly.
 */
@Suppress("TooManyFunctions")
public class SearchResultsView @JvmOverloads constructor(
    outerContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(wrapWithSearchTheme(outerContext), attrs, defStyleAttr) {

    // TODO decouple View from Model
    private lateinit var searchEngine: SearchEngine
    private lateinit var offlineSearchEngine: OfflineSearchEngine

    private val reachabilityInterface: ReachabilityInterface = ReachabilityFactory.reachability(null)
    private var networkReachabilityListenerId: Long = -1

    private var currentSearchRequestTask: SearchRequestTask? = null

    private val historyRecordsInteractor = HistoryRecordsInteractor()
    private var historyRecordsListener: HistoryRecordsInteractor.HistoryListener? = null
    private var historyDelayedLoadingStateChangeTask: Runnable? = null

    /**
     * [SearchOptions] that will be used for search requests.
     */
    public var defaultSearchOptions: SearchOptions = GlobalViewPreferences.DEFAULT_SEARCH_OPTIONS

    private var commonSearchViewConfiguration = CommonSearchViewConfiguration()
    private var isInitialized = false

    /**
     * Search mode of this view, if mode is [SearchMode.ONLINE] [SearchEngine] will be used, [OfflineSearchEngine] otherwise.
     */
    public var searchMode: SearchMode = SearchMode.ONLINE
        set(value) {
            field = value
            isOnlineSearch = value.isOnlineSearch(reachabilityInterface)
        }

    private var isOnlineSearch: Boolean = searchMode.isOnlineSearch(reachabilityInterface)
        set(value) {
            if (value == field) {
                return
            }
            field = value

            logd("isOnlineSearch changed: $value")
            retrySearchRequest()
        }

    private lateinit var searchAdapter: SearchViewResultsAdapter
    private lateinit var itemsCreator: SearchResultsItemsCreator
    private var asyncItemsCreatorTask: SearchCommonAsyncOperationTask? = null

    private val searchCallback = object : SearchSuggestionsCallback, SearchSelectionCallback {
        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            if (searchQuery.isNotEmpty()) {
                showSuggestions(suggestions, responseInfo)
                searchResultListeners.forEach { it.onSuggestions(suggestions, responseInfo) }
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            if (searchQuery.isNotEmpty()) {
                searchResultListeners.forEach { it.onSearchResult(result, responseInfo) }
            }
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            if (searchQuery.isNotEmpty()) {
                showResults(results, responseInfo, fromCategorySuggestion = true)
                searchResultListeners.forEach { it.onCategoryResult(suggestion, results, responseInfo) }
            }
        }

        override fun onError(e: Exception) {
            if (searchQuery.isNotEmpty()) {
                showError(UiError.fromException(e))
                searchResultListeners.forEach { it.onError(e) }
            }
        }
    }

    private val offlineSearchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (searchQuery.isNotEmpty()) {
                showResults(results, responseInfo, fromCategorySuggestion = false)
                searchResultListeners.forEach { it.onOfflineSearchResults(results, responseInfo) }
            }
        }

        override fun onError(e: Exception) {
            if (searchQuery.isNotEmpty()) {
                showError(UiError.fromException(e))
                searchResultListeners.forEach { it.onError(e) }
            }
        }
    }

    private var searchResultsShown = false
    private var searchQuery: String = ""

    private val searchResultListeners = CopyOnWriteArrayList<SearchListener>()
    private val onSuggestionClickListeners = CopyOnWriteArrayList<OnSuggestionClickListener>()

    private val innerSearchResultsCallback = object : SearchViewResultsAdapter.SearchListener {
        override fun onSuggestionItemClicked(searchSuggestion: SearchSuggestion) {
            val processed = onSuggestionClickListeners.any { it.onSuggestionClick(searchSuggestion) }
            if (!processed) {
                onSuggestionSelected(searchSuggestion)
            }
        }

        override fun onResultItemClicked(searchResult: SearchResult, responseInfo: ResponseInfo) {
            searchResultListeners.forEach { it.onSearchResult(searchResult, responseInfo) }
        }

        override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
            searchResultListeners.forEach { it.onHistoryItemClicked(historyRecord) }
        }

        override fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
            searchResultListeners.forEach { it.onPopulateQueryClicked(suggestion, responseInfo) }
        }

        override fun onFeedbackClicked(responseInfo: ResponseInfo) {
            searchResultListeners.forEach { it.onFeedbackClicked(responseInfo) }
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

        searchAdapter = SearchViewResultsAdapter(commonSearchViewConfiguration.distanceUnitType)

        super.setLayoutManager(LinearLayoutManager(context))
        super.setAdapter(searchAdapter)

        searchEngine = MapboxSearchSdk.getSearchEngine()
        offlineSearchEngine = MapboxSearchSdk.getOfflineSearchEngine()

        itemsCreator = SearchResultsItemsCreator(context, searchEngine.settings.locationEngine)

        searchAdapter.searchResultsListener = innerSearchResultsCallback

        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        val helper = ItemTouchHelper(
            HistoryItemSwipeCallback(
                ItemTouchHelper.LEFT, searchAdapter, ::onHistoryRecordRemoved
            )
        )
        helper.attachToRecyclerView(this)

        addItemDecoration(
            OffsetItemDecoration(
                context,
                applyPredicate = { viewHolder ->
                    when (viewHolder) {
                        is SearchHistoryViewHolder,
                        is SearchResultViewHolder -> true
                        else -> false
                    }
                }
            )
        )

        searchAdapter.onRetryClickListener = {
            retrySearchRequest()
        }

        moveToInitialState()
    }

    private fun checkInitialized() {
        check(isInitialized) {
            "Initialize this view first"
        }
    }

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        networkReachabilityListenerId = reachabilityInterface.addListener {
            isOnlineSearch = searchMode.isOnlineSearch(reachabilityInterface)
        }

        if (currentSearchRequestTask?.isCancelled == true) {
            retrySearchRequest()
        }
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        cancelHistoryLoading()

        currentSearchRequestTask?.cancel()

        reachabilityInterface.removeListener(networkReachabilityListenerId)

        super.onDetachedFromWindow()
    }

    private fun moveToInitialState() {
        loadHistory()
    }

    private fun retrySearchRequest() {
        search(searchQuery)
    }

    private fun onHistoryRecordRemoved(adapterPosition: Int, record: HistoryRecord) {
        val newItems = searchAdapter.items.toMutableList().apply {
            removeAt(adapterPosition)
        }
        moveToState(ViewState.History(newItems))

        historyRecordsInteractor.remove(record)
    }

    private fun moveToState(state: ViewState) {
        checkInitialized()

        asyncItemsCreatorTask?.cancel()

        when (state) {
            is ViewState.Results,
            is ViewState.EmptySearchResults,
            is ViewState.Suggestions -> {
                cancelHistoryLoading()
            }
            is ViewState.History -> {
                cancelCurrentNetworkRequest()
            }
            is ViewState.Loading,
            is ViewState.Error -> {
                // Nothing to do here
            }
        }

        searchResultsShown = state is ViewState.Suggestions || state is ViewState.Results
        searchAdapter.items = state.items
    }

    /**
     * Performs forward geocoding.
     * Should be called on the main thread.
     * @param query text to search.
     * @param options options for search request.
     *
     * @throws [IllegalStateException] if [SearchResultsView.initialize] has not been called.
     * @throws [IllegalStateException] if this method is called outside of the main thread.
     */
    @JvmOverloads
    @UiThread
    public fun search(query: String, options: SearchOptions = defaultSearchOptions) {
        checkInitialized()
        checkMainThread()

        searchQuery = query
        if (query.isEmpty()) {
            loadHistory()
        } else {
            if (!searchResultsShown) {
                showLoading()
            }
            cancelHistoryLoading()
            cancelCurrentNetworkRequest()

            currentSearchRequestTask = when (isOnlineSearch) {
                true -> searchEngine.search(query, options, searchCallback)
                false -> offlineSearchEngine.search(query, options.mapToOfflineOptions(), offlineSearchCallback)
            }
        }
    }

    private fun onSuggestionSelected(searchSuggestion: SearchSuggestion) {
        cancelCurrentNetworkRequest()
        currentSearchRequestTask = searchEngine.select(searchSuggestion, searchCallback)
    }

    private fun showLoading() {
        moveToState(ViewState.Loading(itemsCreator.createForLoading()))
    }

    private fun showError(uiError: UiError) {
        moveToState(ViewState.Error(itemsCreator.createForError(uiError)))
    }

    private fun showSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
        if (suggestions.isEmpty()) {
            moveToState(ViewState.EmptySearchResults(itemsCreator.createForEmptySearchResults(responseInfo)))
        } else {
            moveToState(ViewState.Suggestions(itemsCreator.createForSearchSuggestions(suggestions, responseInfo)))
        }
    }

    private fun showResults(results: List<SearchResult>, responseInfo: ResponseInfo, fromCategorySuggestion: Boolean) {
        if (results.isEmpty()) {
            moveToState(ViewState.EmptySearchResults(itemsCreator.createForEmptySearchResults(responseInfo)))
        } else {
            asyncItemsCreatorTask?.cancel()
            asyncItemsCreatorTask = itemsCreator.createForSearchResults(
                results = results,
                responseInfo = responseInfo,
                fromCategorySuggestion = fromCategorySuggestion
            ) { items ->
                moveToState(ViewState.Results(items))
            }
        }
    }

    private fun loadHistory() {
        if (historyRecordsListener != null) {
            return
        }

        var isLoadingCompleted = false

        val listener = object : HistoryRecordsInteractor.HistoryListener {
            override fun onHistoryItems(items: List<Pair<HistoryRecord, Boolean>>) {
                isLoadingCompleted = true
                moveToState(
                    ViewState.History(itemsCreator.createForHistory(items.sortedByDescending { it.first.timestamp }))
                )
            }

            override fun onError(e: Exception) {
                isLoadingCompleted = true
                showError(UiError.UnknownError)
                throwDebug(e) {
                    "Unable to load history records: ${e.message}"
                }
            }
        }

        historyRecordsListener = listener
        historyRecordsInteractor.subscribeToChanges(listener)

        historyDelayedLoadingStateChangeTask = postDelayed(300) {
            if (!isLoadingCompleted) {
                showLoading()
            }
        }
    }

    private fun cancelCurrentNetworkRequest() {
        currentSearchRequestTask?.cancel()
    }

    private fun cancelHistoryLoading() {
        historyDelayedLoadingStateChangeTask?.let {
            removeCallbacks(it)
        }
        historyRecordsListener?.let {
            historyRecordsInteractor.unsubscribe(it)
            historyRecordsListener = null
        }
    }

    /**
     * @suppress
     */
    override fun setLayoutManager(layout: LayoutManager?) {
        throw IllegalStateException("Don't call this function, internal LayoutManager is used")
    }

    /**
     * @suppress
     */
    override fun setAdapter(adapter: Adapter<*>?) {
        throw IllegalStateException("Don't call this function, internal adapter is used")
    }

    /**
     * Adds a listener to be notified of search events.
     *
     * @param listener The listener to notify when a search event happened.
     */
    public fun addSearchListener(listener: SearchListener) {
        searchResultListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeSearchListener(listener: SearchListener) {
        searchResultListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of suggestion clicks.
     *
     * @param listener The listener to notify when a suggestion clicked.
     */
    public fun addOnSuggestionClickListener(listener: OnSuggestionClickListener) {
        onSuggestionClickListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnSuggestionClickListener(listener: OnSuggestionClickListener) {
        onSuggestionClickListeners.remove(listener)
    }

    /**
     * Interface for a listener to be invoked when a suggestion is clicked.
     */
    public fun interface OnSuggestionClickListener {

        /**
         * Called when a suggestion is clicked. This allows listeners to get a chance to process click on their own.
         * [SearchResultsView] will not process the click if this function returns true.
         * If multiple listeners are registered and any of them has processed the click, all the remaining listeners will not be invoked.
         *
         * @param searchSuggestion The clicked SearchSuggestion object.
         * @return True if the listener has processed the click, false otherwise.
         */
        public fun onSuggestionClick(searchSuggestion: SearchSuggestion): Boolean
    }

    /**
     * Search results view listener.
     */
    public interface SearchListener {

        /**
         * Called when the suggestions list is received,
         * i.e. when [SearchSuggestionsCallback.onSuggestions] callback called.
         *
         * @param suggestions List of [SearchSuggestion] as result of the first step of forward geocoding.
         * @param responseInfo Search response and request information.
         *
         * @see SearchSuggestionsCallback.onSuggestions
         */
        public fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo)

        /**
         * Called when a category suggestion has been resolved,
         * i.e. when [SearchSelectionCallback.onCategoryResult] callback called.
         *
         * @param suggestion The category suggestion from which the [results] were resolved.
         * @param results Search results matched by category search.
         * @param responseInfo Search response and request information.
         *
         * @see SearchSelectionCallback.onCategoryResult
         */
        public fun onCategoryResult(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo)

        /**
         * Called when search result is received,
         * i.e. when [SearchSelectionCallback.onResult] callback called.
         *
         * @param searchResult Search result.
         * @param responseInfo Search response and request information.
         *
         * @see SearchSelectionCallback.onResult
         */
        public fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo)

        /**
         * Called when offline search results shown,
         * i.e. when [SearchCallback.onResults] callback called.
         *
         * @param results List of [SearchResult].
         * @param responseInfo Search response and request information.
         *
         * @see SearchCallback.onResults
         */
        public fun onOfflineSearchResults(results: List<SearchResult>, responseInfo: ResponseInfo)

        /**
         * Called if an error occurred during the search request,
         * i.e. when [SearchSuggestionsCallback.onError] callback called.
         *
         * @param e Exception, occurred during the request.
         *
         * @see SearchSuggestionsCallback.onError
         */
        public fun onError(e: Exception)

        /**
         * Called when the history item is clicked.
         * @param historyRecord History item, that selected by user.
         */
        public fun onHistoryItemClicked(historyRecord: HistoryRecord)

        /**
         * Called when search suggestion's "Populate query" button is clicked.
         * @param suggestion Received search suggestion.
         * @param responseInfo Search response and request information.
         */
        public fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo)

        /**
         * Called when "Missing result" button is clicked.
         * @param responseInfo Search response and request information.
         */
        public fun onFeedbackClicked(responseInfo: ResponseInfo)
    }

    private sealed class ViewState {

        abstract val items: List<SearchResultAdapterItem>

        data class History(override val items: List<SearchResultAdapterItem>) : ViewState()
        data class Suggestions(override val items: List<SearchResultAdapterItem>) : ViewState()
        data class Results(override val items: List<SearchResultAdapterItem>) : ViewState()
        data class EmptySearchResults(override val items: List<SearchResultAdapterItem>) : ViewState()
        data class Loading(override val items: List<SearchResultAdapterItem>) : ViewState()
        data class Error(override val items: List<SearchResultAdapterItem>) : ViewState()
    }

    private class HistoryItemSwipeCallback(
        swipeDirs: Int,
        private val adapter: SearchViewResultsAdapter,
        private val onItemSwiped: (Int, HistoryRecord) -> Unit,
    ) : ItemTouchHelper.SimpleCallback(0, swipeDirs) {

        private fun extractHistoryItemOrNull(position: Int): SearchResultAdapterItem.History? {
            if (position < adapter.itemCount) {
                return adapter.items[position] as? SearchResultAdapterItem.History
            }
            return null
        }

        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            return if (extractHistoryItemOrNull(viewHolder.bindingAdapterPosition) == null) {
                0
            } else {
                super.getSwipeDirs(recyclerView, viewHolder)
            }
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: ViewHolder, target: ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val historyItem = extractHistoryItemOrNull(position)
            if (historyItem != null) {
                onItemSwiped(position, historyItem.record)
            }
        }
    }

    private companion object {

        fun SearchOptions.mapToOfflineOptions(): OfflineSearchOptions = OfflineSearchOptions(
            proximity = proximity,
            origin = origin,
            limit = limit,
        )
    }
}
