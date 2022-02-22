package com.mapbox.search.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.UiThread
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.mapbox.common.ReachabilityFactory
import com.mapbox.common.ReachabilityInterface
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.OfflineSearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.common.SearchCommonAsyncOperationTask
import com.mapbox.search.common.concurrent.checkMainThread
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.throwDebug
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.utils.CompoundIndexableDataProvider
import com.mapbox.search.ui.utils.OffsetItemDecoration
import com.mapbox.search.ui.utils.TaskStatus
import com.mapbox.search.ui.utils.extenstion.isCompleted
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.common.UiError
import com.mapbox.search.ui.view.search.SearchHistoryViewHolder
import com.mapbox.search.ui.view.search.SearchResultAdapterItem
import com.mapbox.search.ui.view.search.SearchResultViewHolder
import com.mapbox.search.ui.view.search.SearchResultsItemsCreator
import com.mapbox.search.ui.view.search.SearchViewResultsAdapter
import java.util.concurrent.CopyOnWriteArrayList

internal typealias HistoryFavorites = Pair<List<HistoryRecord>, List<FavoriteRecord>>

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

    private val locationEngine = MapboxSearchSdk.serviceProvider.locationEngine()
    private var currentSearchRequestTask: SearchRequestTask? = null
    private var currentSearchRequestStatus: TaskStatus = TaskStatus.idle()

    private var historyLoadingTask: AsyncOperationTask? = null
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

    private var historyFavoritesDataProvider = CompoundIndexableDataProvider(
        MapboxSearchSdk.serviceProvider.historyDataProvider(),
        MapboxSearchSdk.serviceProvider.favoritesDataProvider()
    )

    private lateinit var searchAdapter: SearchViewResultsAdapter
    private val itemsCreator = SearchResultsItemsCreator(context, locationEngine)
    private var asyncItemsCreatorTask: SearchCommonAsyncOperationTask? = null

    private val searchCallback = object : SearchSuggestionsCallback, SearchSelectionCallback {
        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            currentSearchRequestStatus.markExecuted()
            if (mode.isQueryNotEmpty()) {
                showSuggestions(suggestions, responseInfo)
                searchResultListeners.forEach { it.onSuggestions(suggestions, responseInfo) }
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            currentSearchRequestStatus.markExecuted()
            if (mode.isQueryNotEmpty()) {
                searchResultListeners.forEach { it.onSearchResult(result, responseInfo) }
            }
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            currentSearchRequestStatus.markExecuted()
            if (mode.isQueryNotEmpty()) {
                showResults(results, responseInfo, fromCategorySuggestion = true)
                searchResultListeners.forEach { it.onCategoryResult(suggestion, results, responseInfo) }
            }
        }

        override fun onError(e: Exception) {
            currentSearchRequestStatus.markExecuted()
            if (mode.isQueryNotEmpty()) {
                showError(UiError.fromException(e))
                searchResultListeners.forEach { it.onError(e) }
            }
        }
    }

    private var searchResultsShown = false
    private var mode: Mode = Mode.Query("")

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

        searchAdapter.searchResultsListener = innerSearchResultsCallback

        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

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

        if (currentSearchRequestStatus.isCancelled) {
            retrySearchRequest()
        }
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        cancelHistoryLoading()

        currentSearchRequestStatus.markCancelled()
        currentSearchRequestTask?.cancel()

        reachabilityInterface.removeListener(networkReachabilityListenerId)

        super.onDetachedFromWindow()
    }

    private fun moveToInitialState() {
        loadHistory()
    }

    private fun retrySearchRequest() {
        when (val currentMode = mode) {
            is Mode.Query -> search(currentMode.query)
            is Mode.Suggestion -> searchSuggestion(currentMode.suggestion)
        }
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

        mode = Mode.Query(query)
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
                false -> offlineSearchEngine.search(query, options.mapToOfflineOptions(), searchCallback)
            }
        }
    }

    /*
        TODO
        if we continue searching with suggestion we must be sure that we use the same SearchEngine.
        It's true With the current implementation (in fact MapboxSearchSdk.createSearchEngine() returns the same instance)
        but we should make it more clear and reliable
     */
    @UiThread
    internal fun searchSuggestion(searchSuggestion: SearchSuggestion) {
        checkMainThread()

        cancelHistoryLoading()

        mode = Mode.Suggestion(searchSuggestion)
        showLoading()
        onSuggestionSelected(searchSuggestion)
    }

    private fun onSuggestionSelected(searchSuggestion: SearchSuggestion) {
        cancelCurrentNetworkRequest()

        currentSearchRequestTask = when (isOnlineSearch) {
            true -> searchEngine.select(searchSuggestion, searchCallback)
            false -> offlineSearchEngine.select(searchSuggestion, searchCallback)
        }
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
        fun showHistory(history: List<HistoryRecord>, favorites: List<FavoriteRecord>) {
            /**
             * TODO(web-service/#702):
             *
             * Temporary solution for duplicating HistoryRecord items in the UI list.
             * When a user clicks on a history record, we use only its name to repopulate the search query,
             * so it doesn't make sense to show duplicating items.
             * Moreover, in the UI we show only history record's name so users can't distinguish two records with the same
             * name even if they're actually different.
             */
            val distinctItems = history.distinctBy { it.name }
            moveToState(ViewState.History(itemsCreator.createForHistory(distinctItems, favorites)))
        }

        val currentLoadingTask = historyLoadingTask
        if (currentLoadingTask.isCompleted) {
            return
        }

        var isLoadingCompleted = false

        historyLoadingTask = historyFavoritesDataProvider.getAll(object : CompletionCallback<HistoryFavorites> {
            override fun onComplete(result: HistoryFavorites) {
                isLoadingCompleted = true
                val (history, favorites) = result
                showHistory(history.sortedByDescending { it.timestamp }, favorites)
            }

            override fun onError(e: Exception) {
                isLoadingCompleted = true
                showError(UiError.UnknownError)
                throwDebug(e) {
                    "Unable to load history records"
                }
            }
        })

        historyDelayedLoadingStateChangeTask = postDelayed(300) {
            if (!isLoadingCompleted && !currentLoadingTask.isCompleted) {
                showLoading()
            }
        }
    }

    private fun cancelCurrentNetworkRequest() {
        currentSearchRequestStatus.reset()
        currentSearchRequestTask?.cancel()
    }

    private fun cancelHistoryLoading() {
        historyDelayedLoadingStateChangeTask?.let {
            removeCallbacks(it)
        }
        historyLoadingTask?.cancel()
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

    private sealed class Mode {

        fun isQueryNotEmpty() = !(this is Query && query.isEmpty())

        class Query(val query: String) : Mode()
        class Suggestion(val suggestion: SearchSuggestion) : Mode()
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

    private companion object {

        fun SearchOptions.mapToOfflineOptions(): OfflineSearchOptions = OfflineSearchOptions(
            proximity = proximity,
            origin = origin,
            limit = limit,
        )
    }
}
