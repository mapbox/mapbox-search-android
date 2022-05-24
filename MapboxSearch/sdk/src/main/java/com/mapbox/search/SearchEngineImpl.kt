package com.mapbox.search

import com.mapbox.search.common.assertDebug
import com.mapbox.search.common.logger.logd
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.engine.BaseSearchEngine
import com.mapbox.search.engine.OneStepRequestCallbackWrapper
import com.mapbox.search.engine.TwoStepsBatchRequestCallbackWrapper
import com.mapbox.search.engine.TwoStepsRequestCallbackWrapper
import com.mapbox.search.record.HistoryService
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.BaseSearchSuggestion
import com.mapbox.search.result.GeocodingCompatSearchSuggestion
import com.mapbox.search.result.IndexableRecordSearchResultImpl
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.ServerSearchSuggestion
import com.mapbox.search.result.mapToCore
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

internal class SearchEngineImpl(
    override val apiType: ApiType,
    private val coreEngine: CoreSearchEngineInterface,
    private val historyService: HistoryService,
    private val requestContextProvider: SearchRequestContextProvider,
    private val searchResultFactory: SearchResultFactory,
    private val engineExecutorService: ExecutorService,
    private val indexableDataProvidersRegistry: IndexableDataProvidersRegistry,
) : BaseSearchEngine(), SearchEngine {

    override fun search(
        query: String,
        options: SearchOptions,
        executor: Executor,
        callback: SearchSuggestionsCallback
    ): SearchRequestTask {
        logd("search($query, $options) called")

        return makeRequest(callback) { request ->
            val requestContext = requestContextProvider.provide(apiType)
            coreEngine.search(query, emptyList(), options.mapToCore(),
                TwoStepsRequestCallbackWrapper(
                    apiType = apiType,
                    coreEngine = coreEngine,
                    historyService = historyService,
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContext,
                    suggestion = null,
                    selectOptions = null,
                    isOfflineSearch = false,
                )
            )
        }
    }

    override fun select(
        suggestions: List<SearchSuggestion>,
        executor: Executor,
        callback: SearchMultipleSelectionCallback
    ): SearchRequestTask {
        require(suggestions.isNotEmpty()) {
            "No suggestions were provided! Please, provide at least 1 suggestion."
        }

        if (suggestions.distinctBy { it.requestOptions }.size != 1) {
            executor.execute {
                callback.onError(
                    IllegalArgumentException("All provided suggestions must originate from the same search result!")
                )
            }
            return SearchRequestTaskImpl.completed()
        }

        logd("batch select($suggestions) called")

        val searchResponseInfo = ResponseInfo(suggestions.first().requestOptions, null, isReproducible = false)

        val filtered: List<SearchSuggestion> = suggestions.filter { it.isBatchResolveSupported }
        if (filtered.isEmpty()) {
            executor.execute { callback.onResult(filtered, emptyList(), searchResponseInfo) }
            return SearchRequestTaskImpl.completed()
        }

        logd("Batch retrieve. ${suggestions.size} requested, ${filtered.size} took for processing")

        val alreadyResolved = HashMap<Int, SearchResult>(filtered.size)
        val toResolve = ArrayList<SearchSuggestion>(filtered.size)

        filtered.forEachIndexed { index, suggestion ->
            when (suggestion) {
                is GeocodingCompatSearchSuggestion -> {
                    val result = ServerSearchResultImpl(
                        listOf(suggestion.searchResultType),
                        suggestion.originalSearchResult,
                        suggestion.requestOptions
                    )
                    alreadyResolved[index] = result
                }
                is IndexableRecordSearchSuggestion -> {
                    val resolved = IndexableRecordSearchResultImpl(
                        suggestion.record,
                        suggestion.originalSearchResult,
                        suggestion.requestOptions
                    )

                    alreadyResolved[index] = resolved
                }
                is ServerSearchSuggestion -> {
                    toResolve.add(suggestion)
                }
            }
        }

        return when (alreadyResolved.size) {
            filtered.size -> {
                val result = filtered.indices.mapNotNull { alreadyResolved[it] }
                executor.execute {
                    callback.onResult(filtered, result, searchResponseInfo)
                }
                SearchRequestTaskImpl.completed()
            }
            else -> {
                val coreSearchResults = toResolve.map {
                    (it as BaseSearchSuggestion).originalSearchResult.mapToCore()
                }

                val resultingFunction: (List<SearchResult>) -> List<SearchResult> = { remoteResults ->
                    assertDebug(remoteResults.size == toResolve.size) {
                        "Not all items have been resolved. " +
                                "To resolve: ${toResolve.map { it.id to it.type }}, " +
                                "actual: ${remoteResults.map { it.id to it.types }}"
                    }
                    if (remoteResults.size != toResolve.size) {
                        alreadyResolved.values + remoteResults
                    } else {
                        val finalSize = alreadyResolved.size + remoteResults.size
                        val result = ArrayList<SearchResult>(finalSize)

                        var remoteResultsIndex = 0
                        (0 until finalSize).map { index ->
                            val element = if (alreadyResolved[index] != null) {
                                alreadyResolved[index]!!
                            } else {
                                remoteResults[remoteResultsIndex++]
                            }
                            result.add(element)
                        }
                        result
                    }
                }

                makeRequest(callback) { searchRequestTask ->
                    val requestOptions = toResolve.first().requestOptions
                    val requestContext = requestOptions.requestContext
                    coreEngine.retrieveBucket(
                        requestOptions.mapToCore(),
                        coreSearchResults,
                        TwoStepsBatchRequestCallbackWrapper(
                            suggestions = filtered,
                            searchResultFactory = searchResultFactory,
                            callbackExecutor = executor,
                            workerExecutor = engineExecutorService,
                            searchRequestTask = searchRequestTask,
                            resultingFunction = resultingFunction,
                            searchRequestContext = requestContext
                        )
                    )
                }
            }
        }
    }

    @Suppress("ReturnCount")
    override fun select(
        suggestion: SearchSuggestion,
        options: SelectOptions,
        executor: Executor,
        callback: SearchSelectionCallback
    ): SearchRequestTask {
        logd("select($suggestion, $options) called")

        val coreRequestOptions = suggestion.requestOptions.mapToCore()

        fun completeSearchResultSelection(
            suggestion: BaseSearchSuggestion,
            resolved: SearchResult
        ): SearchRequestTask {
            val searchRequestTask = SearchRequestTaskImpl(callback)

            coreEngine.onSelected(coreRequestOptions, suggestion.originalSearchResult.mapToCore())

            val responseInfo = ResponseInfo(
                requestOptions = suggestion.requestOptions,
                coreSearchResponse = null,
                isReproducible = false
            )

            if (!options.addResultToHistory) {
                searchRequestTask.markExecutedAndRunOnCallback(executor) {
                    onResult(suggestion, resolved, responseInfo)
                }
                return searchRequestTask
            }

            if (!searchRequestTask.isCompleted) {
                searchRequestTask += historyService.addToHistoryIfNeeded(
                    searchResult = resolved,
                    callback = object : CompletionCallback<Boolean> {
                        override fun onComplete(result: Boolean) {
                            searchRequestTask.markExecutedAndRunOnCallback(executor) {
                                onResult(suggestion, resolved, responseInfo)
                            }
                        }

                        override fun onError(e: Exception) {
                            searchRequestTask.markExecutedAndRunOnCallback(executor) {
                                onError(e)
                            }
                        }
                    }
                )
            }
            return searchRequestTask
        }

        return when (suggestion) {
            is GeocodingCompatSearchSuggestion -> {
                val searchResult = ServerSearchResultImpl(
                    listOf(suggestion.searchResultType),
                    suggestion.originalSearchResult,
                    suggestion.requestOptions
                )
                completeSearchResultSelection(suggestion, searchResult)
            }
            is ServerSearchSuggestion -> makeRequest<SearchSuggestionsCallback>(callback) { request ->
                val requestContext = suggestion.requestOptions.requestContext
                coreEngine.retrieve(
                    coreRequestOptions,
                    suggestion.originalSearchResult.mapToCore(),
                    TwoStepsRequestCallbackWrapper(
                        apiType = apiType,
                        coreEngine = coreEngine,
                        historyService = historyService,
                        searchResultFactory = searchResultFactory,
                        callbackExecutor = executor,
                        workerExecutor = engineExecutorService,
                        searchRequestTask = request,
                        searchRequestContext = requestContext,
                        suggestion = suggestion,
                        selectOptions = options,
                        isOfflineSearch = false,
                    )
                )
            }
            is IndexableRecordSearchSuggestion -> {
                val resolved = IndexableRecordSearchResultImpl(
                    suggestion.record,
                    suggestion.originalSearchResult,
                    suggestion.requestOptions
                )
                completeSearchResultSelection(suggestion, resolved)
            }
            is BaseSearchSuggestion -> {
                error("Unprocessed suggestion: $suggestion")
            }
        }
    }

    override fun search(
        categoryName: String,
        options: CategorySearchOptions,
        executor: Executor,
        callback: SearchCallback,
    ): SearchRequestTask {
        return makeRequest(callback) { request ->
            val requestContext = requestContextProvider.provide(apiType)
            coreEngine.search(
                "",
                listOf(categoryName),
                options.mapToCoreCategory(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContext,
                    isOffline = false,
                )
            )
        }
    }

    override fun search(
        options: ReverseGeoOptions,
        executor: Executor,
        callback: SearchCallback
    ): SearchRequestTask {
        return makeRequest(callback) { request ->
            val requestContext = requestContextProvider.provide(apiType)
            coreEngine.reverseGeocoding(
                options.mapToCore(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = request,
                    searchRequestContext = requestContext,
                    isOffline = false,
                )
            )
        }
    }

    override fun <R : IndexableRecord> registerDataProvider(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        return indexableDataProvidersRegistry.register(
            dataProvider = dataProvider,
            searchEngine = coreEngine,
            executor = executor,
            callback = callback,
        )
    }

    override fun <R : IndexableRecord> unregisterDataProvider(
        dataProvider: IndexableDataProvider<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        return indexableDataProvidersRegistry.unregister(
            dataProvider = dataProvider,
            searchEngine = coreEngine,
            executor = executor,
            callback = callback,
        )
    }
}
