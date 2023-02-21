package com.mapbox.search.ui.adapter.engines

import android.Manifest
import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.annotation.UiThread
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.common.ReachabilityFactory
import com.mapbox.common.ReachabilityInterface
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.ServiceProvider
import com.mapbox.search.base.concurrent.checkMainThread
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.location.defaultLocationEngine
import com.mapbox.search.base.logger.logd
import com.mapbox.search.base.throwDebug
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.internal.bindgen.UserActivityReporter
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchOptions
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.utils.HistoryRecordsInteractor
import com.mapbox.search.ui.utils.offline.mapToSdkSearchResultType
import com.mapbox.search.ui.view.GlobalViewPreferences
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.UiError
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Helper class that implements search-specific logic and shows search results on the [SearchResultsView].
 */
public class SearchEngineUiAdapter(

    /**
     * [SearchResultsView] for displaying search results.
     */
    private val view: SearchResultsView,

    /**
     * Search engine for online mode.
     * @see [searchMode]
     */
    private val searchEngine: SearchEngine,

    /**
     * Search engine for offline mode.
     * @see [searchMode]
     */
    private val offlineSearchEngine: OfflineSearchEngine,

    /**
     * The mechanism responsible for providing location approximations to the SDK.
     * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
     * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
     * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     */
    locationEngine: LocationEngine = defaultLocationEngine(),

    /**
     * Search history engine. Selected search results will automatically be added to the provided [HistoryDataProvider].
     */
    private val historyDataProvider: HistoryDataProvider = ServiceProvider.INSTANCE.historyDataProvider()
) {

    private val searchListeners = CopyOnWriteArrayList<SearchListener>()

    private var searchResultsShown = false
    private var searchQuery: String = ""
    private var latestSearchOptions: SearchOptions = GlobalViewPreferences.DEFAULT_SEARCH_OPTIONS

    private val reachabilityInterface: ReachabilityInterface = ReachabilityFactory.reachability(null)
    private var networkReachabilityListenerId: Long = -1

    private var currentSearchRequestTask: AsyncOperationTask? = null

    private val historyRecordsInteractor = HistoryRecordsInteractor()
    private var historyRecordsListener: HistoryRecordsInteractor.HistoryListener? = null
    private var historyDelayedLoadingStateChangeTask: Runnable? = null

    private val itemsCreator: SearchResultsItemsCreator
    private var asyncItemsCreatorTask: AsyncOperationTask? = null

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

    private val searchCallback = object : SearchSuggestionsCallback, SearchSelectionCallback {

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            if (searchQuery.isNotEmpty()) {
                showSuggestions(suggestions, responseInfo)
                searchListeners.forEach { it.onSuggestionsShown(suggestions, responseInfo) }
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            if (searchQuery.isNotEmpty()) {
                searchListeners.forEach { it.onSearchResultSelected(result, responseInfo) }
            }
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            if (searchQuery.isNotEmpty()) {
                showResults(results, responseInfo)
                searchListeners.forEach { it.onCategoryResultsShown(suggestion, results, responseInfo) }
            }
        }

        override fun onError(e: Exception) {
            if (searchQuery.isNotEmpty()) {
                showError(UiError.createFromException(e))
                searchListeners.forEach { it.onError(e) }
            }
        }
    }

    private val offlineSearchCallback = object : OfflineSearchCallback {

        override fun onResults(
            results: List<OfflineSearchResult>,
            responseInfo: OfflineResponseInfo
        ) {
            if (searchQuery.isNotEmpty()) {
                showResults(results, responseInfo)
                searchListeners.forEach { it.onOfflineSearchResultsShown(results, responseInfo) }
            }
        }

        override fun onError(e: Exception) {
            if (searchQuery.isNotEmpty()) {
                showError(UiError.createFromException(e))
                searchListeners.forEach { it.onError(e) }
            }
        }
    }

    private val activityReporter: UserActivityReporter = getUserActivityReporter(searchEngine.settings.accessToken)

    init {
        val helper = ItemTouchHelper(
            HistoryItemSwipeCallback(
                ItemTouchHelper.LEFT, view, ::onHistoryRecordRemoved
            )
        )
        helper.attachToRecyclerView(view)

        itemsCreator = SearchResultsItemsCreator(
            context = view.context,
            locationEngine = locationEngine
        )

        view.addActionListener(object : SearchResultsView.ActionListener {

            override fun onHistoryItemClick(item: SearchResultAdapterItem.History) {
                searchListeners.forEach { it.onHistoryItemClick(item.record) }
            }

            override fun onResultItemClick(item: SearchResultAdapterItem.Result) {
                val payload = item.payload
                if (payload !is Pair<*, *>) {
                    failDebug {
                        "Unknown adapter item payload: $payload"
                    }
                    return
                }

                val (first, second) = payload
                when {
                    first is OfflineSearchResult && second is OfflineResponseInfo -> {
                        addToHistoryIfNeeded(first)
                        searchListeners.forEach { it.onOfflineSearchResultSelected(first, second) }
                    }
                    first is SearchResult && second is ResponseInfo -> {
                        addToHistoryIfNeeded(first)
                        searchListeners.forEach { it.onSearchResultSelected(first, second) }
                    }
                    first is SearchSuggestion && second is ResponseInfo -> {
                        val processed = searchListeners.any { it.onSuggestionSelected(first) }
                        if (!processed) {
                            onSuggestionSelected(first)
                        }
                    }
                    else -> {
                        failDebug {
                            "Unknown adapter item payload: $payload"
                        }
                    }
                }
            }

            override fun onPopulateQueryClick(item: SearchResultAdapterItem.Result) {
                val payload = item.payload
                if (payload is Pair<*, *>) {
                    val (first, second) = payload
                    when {
                        first is SearchSuggestion && second is ResponseInfo -> {
                            searchListeners.forEach { it.onPopulateQueryClick(first, second) }
                        }
                    }
                } else {
                    failDebug {
                        "Unknown adapter item payload: $payload"
                    }
                }
            }

            override fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback) {
                searchListeners.forEach { it.onFeedbackItemClick(item.responseInfo) }
            }

            override fun onErrorItemClick(item: SearchResultAdapterItem.Error) {
                retrySearchRequest()
            }
        })

        view.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAttachedToWindow()
            }

            override fun onViewDetachedFromWindow(v: View) {
                onDetachedFromWindow()
            }
        })

        loadHistory()
    }

    /**
     * Performs forward geocoding.
     * Should be called on the main thread.
     * @param query text to search.
     * @param options options for search request.
     *
     * @throws [IllegalStateException] if this method is called outside of the main thread.
     */
    @JvmOverloads
    @UiThread
    public fun search(
        query: String,
        options: SearchOptions = GlobalViewPreferences.DEFAULT_SEARCH_OPTIONS,
    ) {
        checkMainThread()

        searchQuery = query
        latestSearchOptions = options

        if (query.isEmpty()) {
            loadHistory()
        } else {
            if (!searchResultsShown) {
                showLoading()
            }
            cancelHistoryLoading()
            cancelCurrentNetworkRequest()

            currentSearchRequestTask = when (isOnlineSearch) {
                true -> {
                    activityReporter.reportActivity("search-engine-forward-geocoding-suggestions-ui")
                    searchEngine.search(query, options, searchCallback)
                }
                false -> {
                    activityReporter.reportActivity("offline-search-engine-forward-geocoding-ui")
                    offlineSearchEngine.search(
                        query,
                        options.mapToOfflineOptions(),
                        offlineSearchCallback
                    )
                }
            }
        }
    }

    private fun onHistoryRecordRemoved(adapterPosition: Int, record: HistoryRecord) {
        val newItems = view.adapterItems.toMutableList().apply {
            removeAt(adapterPosition)
        }
        showHistoryItems(newItems)

        historyRecordsInteractor.remove(record)
    }

    private fun loadHistory() {
        if (historyRecordsListener != null) {
            return
        }

        var isLoadingCompleted = false

        val listener = object : HistoryRecordsInteractor.HistoryListener {
            override fun onHistoryItems(items: List<Pair<HistoryRecord, Boolean>>) {
                isLoadingCompleted = true
                showHistory(items.sortedByDescending { it.first.timestamp })
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

        historyDelayedLoadingStateChangeTask = view.postDelayed(300) {
            if (!isLoadingCompleted) {
                showLoading()
            }
        }
    }

    private fun showError(uiError: UiError) {
        asyncItemsCreatorTask?.cancel()
        view.setAdapterItems(itemsCreator.createForError(uiError))
        searchResultsShown = false
    }

    private fun showLoading() {
        asyncItemsCreatorTask?.cancel()
        view.setAdapterItems(itemsCreator.createForLoading())
        searchResultsShown = false
    }

    private fun showSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
        cancelHistoryLoading()
        asyncItemsCreatorTask?.cancel()
        view.setAdapterItems(itemsCreator.createForSearchSuggestions(suggestions, responseInfo))
        searchResultsShown = suggestions.isNotEmpty()
    }

    private fun showResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
        cancelHistoryLoading()
        asyncItemsCreatorTask?.cancel()
        asyncItemsCreatorTask = itemsCreator.createForSearchResults(
            results = results,
            responseInfo = responseInfo,
        ) {
            view.setAdapterItems(it)
            searchResultsShown = results.isNotEmpty()
        }
    }

    private fun showResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
        cancelHistoryLoading()
        asyncItemsCreatorTask?.cancel()
        asyncItemsCreatorTask = itemsCreator.createForOfflineSearchResults(
            results = results,
            responseInfo = responseInfo,
        ) {
            view.setAdapterItems(it)
            searchResultsShown = results.isNotEmpty()
        }
    }

    private fun showHistory(history: List<Pair<HistoryRecord, Boolean>>) {
        asyncItemsCreatorTask?.cancel()
        cancelCurrentNetworkRequest()
        view.setAdapterItems(itemsCreator.createForHistory(history))
        searchResultsShown = false
    }

    private fun showHistoryItems(items: List<SearchResultAdapterItem>) {
        view.setAdapterItems(items)
        cancelCurrentNetworkRequest()
        searchResultsShown = false
    }

    private fun onAttachedToWindow() {
        networkReachabilityListenerId = reachabilityInterface.addListener {
            isOnlineSearch = searchMode.isOnlineSearch(reachabilityInterface)
        }

        if (currentSearchRequestTask?.isCancelled == true) {
            retrySearchRequest()
        }
    }

    private fun onDetachedFromWindow() {
        cancelHistoryLoading()

        currentSearchRequestTask?.cancel()

        reachabilityInterface.removeListener(networkReachabilityListenerId)
    }

    private fun retrySearchRequest() {
        search(searchQuery, latestSearchOptions)
    }

    private fun cancelCurrentNetworkRequest() {
        currentSearchRequestTask?.cancel()
    }

    private fun cancelHistoryLoading() {
        historyDelayedLoadingStateChangeTask?.let {
            view.removeCallbacks(it)
        }
        historyRecordsListener?.let {
            historyRecordsInteractor.unsubscribe(it)
            historyRecordsListener = null
        }
    }

    private fun onSuggestionSelected(searchSuggestion: SearchSuggestion) {
        cancelCurrentNetworkRequest()
        activityReporter.reportActivity("search-engine-forward-geocoding-selection-ui")
        currentSearchRequestTask = searchEngine.select(searchSuggestion, searchCallback)
    }

    private fun addToHistoryIfNeeded(searchResult: SearchResult) {
        if (searchResult.indexableRecord is HistoryRecord) return

        HistoryRecord(
            id = searchResult.id,
            name = searchResult.name,
            descriptionText = searchResult.descriptionText,
            address = searchResult.address,
            routablePoints = searchResult.routablePoints,
            categories = searchResult.categories,
            makiIcon = searchResult.makiIcon,
            coordinate = searchResult.coordinate,
            type = searchResult.types.first(),
            metadata = searchResult.metadata,
            timestamp = System.currentTimeMillis(),
        ).also {
            addToHistoryIfNeeded(it)
        }
    }

    private fun addToHistoryIfNeeded(searchResult: OfflineSearchResult) {
        HistoryRecord(
            id = searchResult.id,
            name = searchResult.name,
            descriptionText = searchResult.descriptionText,
            address = searchResult.address?.mapToSdkSearchResultType(),
            routablePoints = searchResult.routablePoints,
            categories = null,
            makiIcon = null,
            coordinate = searchResult.coordinate,
            type = searchResult.type.mapToSdkSearchResultType(),
            metadata = null,
            timestamp = System.currentTimeMillis(),
        ).also {
            addToHistoryIfNeeded(it)
        }
    }

    private fun addToHistoryIfNeeded(historyRecord: HistoryRecord) {
        historyDataProvider.upsert(
            historyRecord,
            object : CompletionCallback<Unit> {
                override fun onComplete(result: Unit) {
                    logd("Search result added to history")
                }

                override fun onError(e: Exception) {
                    logd("Unable to add SearchResult to history: ${e.message}")
                }
            }
        )
    }

    /**
     * Adds a listener to be notified of search events.
     *
     * @param listener The listener to notify when a search event happened.
     */
    public fun addSearchListener(listener: SearchListener) {
        searchListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeSearchListener(listener: SearchListener) {
        searchListeners.remove(listener)
    }

    private class HistoryItemSwipeCallback(
        swipeDirs: Int,
        private val view: SearchResultsView,
        private val onItemSwiped: (Int, HistoryRecord) -> Unit,
    ) : ItemTouchHelper.SimpleCallback(0, swipeDirs) {

        private fun extractHistoryItemOrNull(position: Int): SearchResultAdapterItem.History? {
            if (position < view.adapterItems.size) {
                return view.adapterItems[position] as? SearchResultAdapterItem.History
            }
            return null
        }

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return if (extractHistoryItemOrNull(viewHolder.bindingAdapterPosition) == null) {
                0
            } else {
                super.getSwipeDirs(recyclerView, viewHolder)
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val historyItem = extractHistoryItemOrNull(position)
            if (historyItem != null) {
                onItemSwiped(position, historyItem.record)
            }
        }
    }

    /**
     * Search results view listener.
     */
    public interface SearchListener {

        /**
         * Called when the suggestions are received and displayed on the [view].
         * This happens when [SearchSuggestionsCallback.onSuggestions] callback called.
         *
         * @param suggestions List of [SearchSuggestion] as result of the first step of forward geocoding.
         * @param responseInfo Search response and request information.
         *
         * @see SearchSuggestionsCallback.onSuggestions
         */
        public fun onSuggestionsShown(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo)

        /**
         * Called when a category results are resolved and displayed on the [view].
         * This happens when [SearchSelectionCallback.onCategoryResult] callback called.
         *
         * @param suggestion The category suggestion from which the [results] were resolved.
         * @param results Search results matched by category search.
         * @param responseInfo Search response and request information.
         *
         * @see SearchSelectionCallback.onCategoryResult
         */
        public fun onCategoryResultsShown(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        )

        /**
         * Called when offline search results shown, i.e. when [OfflineSearchCallback.onResults] callback called.
         *
         * @param results List of [OfflineSearchResult].
         * @param responseInfo Search response and request information.
         *
         * @see OfflineSearchCallback.onResults
         */
        public fun onOfflineSearchResultsShown(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo)

        /**
         * Called when a suggestion is clicked. This allows listeners to get a chance to process click on their own.
         * [SearchResultsView] will not process the click if this function returns true.
         * If multiple listeners are registered and any of them has processed the click,
         * all the remaining listeners will not be invoked.
         *
         * If non of the added listeners processed the selection,
         * this [SearchEngineUiAdapter] will proceed with the [SearchEngine.select] call.
         *
         * @param searchSuggestion The clicked SearchSuggestion object.
         * @return True if the listener has processed the click, false otherwise.
         */
        public fun onSuggestionSelected(searchSuggestion: SearchSuggestion): Boolean

        /**
         * Called when a user retrieves [SearchResult] from one of currently displayed on the [view] [SearchSuggestion]s
         * (i.e. when [SearchSelectionCallback.onResult] callback is called) or when the user selects one of the
         * currently displayed [SearchResult]'s.
         *
         * @param searchResult Search result.
         * @param responseInfo Search response and request information.
         *
         * @see SearchSelectionCallback.onResult
         */
        public fun onSearchResultSelected(searchResult: SearchResult, responseInfo: ResponseInfo)

        /**
         Called when a user when a user selects one of the currently displayed [OfflineSearchResult]s.
         *
         * @param searchResult Search result.
         * @param responseInfo Search response and request information.
         */
        public fun onOfflineSearchResultSelected(searchResult: OfflineSearchResult, responseInfo: OfflineResponseInfo)

        /**
         * Called when error occurs during the search request,
         * i.e. when one of [SearchSuggestionsCallback.onError], [SearchSelectionCallback.onError],
         * [OfflineSearchCallback.onError] callbacks called.
         * When this happens, error information is displayed on the [view].
         *
         * @param e Exception, occurred during the request.
         * @see SearchSuggestionsCallback.onError
         * @see SearchSelectionCallback.onError
         * @see OfflineSearchCallback.onError
         */
        public fun onError(e: Exception)

        /**
         * Called when a history item is clicked.
         * @param historyRecord History item, that selected by user.
         */
        public fun onHistoryItemClick(historyRecord: HistoryRecord)

        /**
         * Called when search suggestion's "Populate query" button is clicked.
         *
         * @param suggestion Received search suggestion.
         * @param responseInfo Search response and request information.
         */
        public fun onPopulateQueryClick(suggestion: SearchSuggestion, responseInfo: ResponseInfo)

        /**
         * Called when "Missing result" button is clicked.
         * @param responseInfo Search response and request information.
         */
        public fun onFeedbackItemClick(responseInfo: ResponseInfo)
    }

    private companion object {
        fun SearchOptions.mapToOfflineOptions(): OfflineSearchOptions = OfflineSearchOptions(
            proximity = proximity,
            origin = origin,
            limit = limit,
        )
    }
}
