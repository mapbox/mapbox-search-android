package com.mapbox.search

import com.mapbox.search.adapter.BaseSearchCallbackAdapter
import com.mapbox.search.adapter.BaseSearchMultipleSelectionCallbackAdapter
import com.mapbox.search.adapter.BaseSearchSelectionCallbackAdapter
import com.mapbox.search.adapter.BaseSearchSuggestionsCallbackAdapter
import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.base.BaseSearchMultipleSelectionCallback
import com.mapbox.search.base.BaseSearchSuggestionsCallback
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.engine.BaseSearchEngine
import com.mapbox.search.base.engine.OneStepRequestCallbackWrapper
import com.mapbox.search.base.engine.TwoStepsBatchRequestCallbackWrapper
import com.mapbox.search.base.engine.TwoStepsRequestCallbackWrapper
import com.mapbox.search.base.logger.logd
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseGeocodingCompatSearchSuggestion
import com.mapbox.search.base.result.BaseIndexableRecordSearchResultImpl
import com.mapbox.search.base.result.BaseIndexableRecordSearchSuggestion
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToCore
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class SearchEngineImpl(
    override val apiType: ApiType,
    override val settings: SearchEngineSettings,
    override val analyticsService: AnalyticsService,
    private val coreEngine: CoreSearchEngineInterface,
    private val historyService: SearchHistoryService,
    private val requestContextProvider: SearchRequestContextProvider,
    private val searchResultFactory: SearchResultFactory,
    private val engineExecutorService: ExecutorService = DEFAULT_EXECUTOR,
    private val indexableDataProvidersRegistry: IndexableDataProvidersRegistry,
) : BaseSearchEngine(), SearchEngine {

    override fun search(
        query: String,
        options: SearchOptions,
        executor: Executor,
        callback: SearchSuggestionsCallback
    ): AsyncOperationTask {
        logd("search($query, $options) called")

        val baseCallback: BaseSearchSuggestionsCallback = BaseSearchSuggestionsCallbackAdapter(callback)
        return makeRequest(baseCallback) { task ->
            val requestContext = requestContextProvider.provide(apiType.mapToCore())
            val requestId = coreEngine.search(
                query, emptyList(), options.mapToCore(),
                TwoStepsRequestCallbackWrapper(
                    apiType = apiType.mapToCore(),
                    coreEngine = coreEngine,
                    historyService = historyService,
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = task,
                    searchRequestContext = requestContext,
                    suggestion = null,
                    addResultToHistory = false,
                )
            )
            task.addOnCancelledCallback {
                coreEngine.cancel(requestId)
            }
        }
    }

    override fun select(
        suggestions: List<SearchSuggestion>,
        executor: Executor,
        callback: SearchMultipleSelectionCallback
    ): AsyncOperationTask {
        require(suggestions.isNotEmpty()) {
            "No suggestions were provided! Please, provide at least 1 suggestion."
        }

        if (suggestions.distinctBy { it.requestOptions }.size != 1) {
            executor.execute {
                callback.onError(
                    IllegalArgumentException("All provided suggestions must originate from the same search result!")
                )
            }
            return AsyncOperationTaskImpl.COMPLETED
        }

        logd("batch select($suggestions) called")

        val searchResponseInfo = ResponseInfo(suggestions.first().requestOptions, null, isReproducible = false)

        val filtered: List<SearchSuggestion> = suggestions.filter { it.isBatchResolveSupported }
        if (filtered.isEmpty()) {
            executor.execute { callback.onResult(filtered, emptyList(), searchResponseInfo) }
            return AsyncOperationTaskImpl.COMPLETED
        }

        logd("Batch retrieve. ${suggestions.size} requested, ${filtered.size} took for processing")

        val alreadyResolved = HashMap<Int, BaseSearchResult>(filtered.size)
        val toResolve = ArrayList<BaseSearchSuggestion>(filtered.size)

        filtered.forEachIndexed { index, suggestion ->
            when (val base = suggestion.base) {
                is BaseGeocodingCompatSearchSuggestion -> {
                    val result = BaseServerSearchResultImpl(
                        listOf(base.searchResultType),
                        base.rawSearchResult,
                        base.requestOptions
                    )
                    alreadyResolved[index] = result
                }
                is BaseIndexableRecordSearchSuggestion -> {
                    val resolved = BaseIndexableRecordSearchResultImpl(
                        base.record,
                        base.rawSearchResult,
                        base.requestOptions
                    )

                    alreadyResolved[index] = resolved
                }
                is BaseServerSearchSuggestion -> {
                    toResolve.add(base)
                }
            }
        }

        return when (alreadyResolved.size) {
            filtered.size -> {
                val result = filtered.indices.mapNotNull { index ->
                    alreadyResolved[index]?.let { SearchResult(it) }
                }
                executor.execute {
                    callback.onResult(filtered, result, searchResponseInfo)
                }
                AsyncOperationTaskImpl.COMPLETED
            }
            else -> {
                val coreSearchResults = toResolve.map {
                    it.rawSearchResult.mapToCore()
                }

                val resultingFunction: (List<BaseSearchResult>) -> List<BaseSearchResult> = { remoteResults ->
                    assertDebug(remoteResults.size == toResolve.size) {
                        "Not all items have been resolved. " +
                                "To resolve: ${toResolve.map { it.id to it.type }}, " +
                                "actual: ${remoteResults.map { it.id to it.types }}"
                    }
                    if (remoteResults.size != toResolve.size) {
                        alreadyResolved.values + remoteResults
                    } else {
                        val finalSize = alreadyResolved.size + remoteResults.size
                        val result = ArrayList<BaseSearchResult>(finalSize)

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

                val baseCallback: BaseSearchMultipleSelectionCallback = BaseSearchMultipleSelectionCallbackAdapter(callback)
                makeRequest(baseCallback) { task ->
                    val requestOptions = toResolve.first().requestOptions
                    val requestContext = requestOptions.requestContext
                    val requestId = coreEngine.retrieveBucket(
                        requestOptions.core,
                        coreSearchResults,
                        TwoStepsBatchRequestCallbackWrapper(
                            suggestions = filtered.map { it.base },
                            searchResultFactory = searchResultFactory,
                            callbackExecutor = executor,
                            workerExecutor = engineExecutorService,
                            searchRequestTask = task,
                            resultingFunction = resultingFunction,
                            searchRequestContext = requestContext,
                        )
                    )
                    task.addOnCancelledCallback {
                        coreEngine.cancel(requestId)
                    }
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
    ): AsyncOperationTask {
        logd("select($suggestion, $options) called")

        val coreRequestOptions = suggestion.requestOptions.mapToCore()

        fun completeSearchResultSelection(
            suggestion: SearchSuggestion,
            resolved: SearchResult
        ): AsyncOperationTask {
            val task = AsyncOperationTaskImpl(callback)

            coreEngine.onSelected(coreRequestOptions, suggestion.base.rawSearchResult.mapToCore())

            val responseInfo = ResponseInfo(
                requestOptions = suggestion.requestOptions,
                coreSearchResponse = null,
                isReproducible = false
            )

            if (!options.addResultToHistory) {
                task.markExecutedAndRunOnCallback(executor) {
                    onResult(suggestion, resolved, responseInfo)
                }
                return task
            }

            if (!task.isCompleted) {
                task += historyService.addToHistoryIfNeeded(
                    searchResult = resolved.base,
                    callback = { result ->
                        result.onSuccess {
                            task.markExecutedAndRunOnCallback(executor) {
                                onResult(suggestion, resolved, responseInfo)
                            }
                        }.onFailure { e ->
                            task.markExecutedAndRunOnCallback(executor) {
                                onError((e as? Exception) ?: Exception(e))
                            }
                        }
                    }
                )
            }
            return task
        }

        return when (val base = suggestion.base) {
            is BaseGeocodingCompatSearchSuggestion -> {
                val baseSearchResult = BaseServerSearchResultImpl(
                    types = listOf(base.searchResultType),
                    rawSearchResult = base.rawSearchResult,
                    requestOptions = suggestion.requestOptions.mapToBase()
                )
                completeSearchResultSelection(suggestion, SearchResult(baseSearchResult))
            }
            is BaseServerSearchSuggestion -> {
                val baseCallback: BaseSearchSuggestionsCallback = BaseSearchSelectionCallbackAdapter(callback)
                makeRequest(baseCallback) { task ->
                    val requestContext = suggestion.requestOptions.requestContext
                    val requestId = coreEngine.retrieve(
                        coreRequestOptions,
                        base.rawSearchResult.mapToCore(),
                        TwoStepsRequestCallbackWrapper(
                            apiType = apiType.mapToCore(),
                            coreEngine = coreEngine,
                            historyService = historyService,
                            searchResultFactory = searchResultFactory,
                            callbackExecutor = executor,
                            workerExecutor = engineExecutorService,
                            searchRequestTask = task,
                            searchRequestContext = requestContext,
                            suggestion = suggestion.base,
                            addResultToHistory = options.addResultToHistory,
                        )
                    )
                    task.addOnCancelledCallback {
                        coreEngine.cancel(requestId)
                    }
                }
            }
            is BaseIndexableRecordSearchSuggestion -> {
                val baseSearchResult = BaseIndexableRecordSearchResultImpl(
                    record = base.record,
                    rawSearchResult = base.rawSearchResult,
                    requestOptions = suggestion.requestOptions.mapToBase()
                )
                completeSearchResultSelection(suggestion, SearchResult(baseSearchResult))
            }
        }
    }

    override fun search(
        categoryName: String,
        options: CategorySearchOptions,
        executor: Executor,
        callback: SearchCallback,
    ): AsyncOperationTask {
        return makeRequest(BaseSearchCallbackAdapter(callback)) { task ->
            val requestContext = requestContextProvider.provide(apiType.mapToCore())
            val requestId = coreEngine.search(
                "",
                listOf(categoryName),
                options.mapToCoreCategory(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = task,
                    searchRequestContext = requestContext,
                    isOffline = false,
                )
            )
            task.addOnCancelledCallback {
                coreEngine.cancel(requestId)
            }
        }
    }

    override fun search(
        options: ReverseGeoOptions,
        executor: Executor,
        callback: SearchCallback
    ): AsyncOperationTask {
        return makeRequest(BaseSearchCallbackAdapter(callback)) { task ->
            val requestContext = requestContextProvider.provide(apiType.mapToCore())
            val requestId = coreEngine.reverseGeocoding(
                options.mapToCore(),
                OneStepRequestCallbackWrapper(
                    searchResultFactory = searchResultFactory,
                    callbackExecutor = executor,
                    workerExecutor = engineExecutorService,
                    searchRequestTask = task,
                    searchRequestContext = requestContext,
                    isOffline = false,
                )
            )
            task.addOnCancelledCallback {
                coreEngine.cancel(requestId)
            }
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

    private companion object {
        val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "SearchEngine executor")
        }
    }
}
