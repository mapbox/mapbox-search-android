package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.reportError
import com.mapbox.search.core.CoreRequestOptions
import com.mapbox.search.core.CoreSearchCallback
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.CoreSearchOptions
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.core.CoreSearchResult
import com.mapbox.search.core.http.HttpErrorsCache
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.internal.bindgen.SearchAddress
import com.mapbox.search.record.DataProviderResolver
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryService
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.GeocodingCompatSearchSuggestion
import com.mapbox.search.result.IndexableRecordSearchResultImpl
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.OriginalResultType
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.ServerSearchSuggestion
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.tests_support.TestExecutor
import com.mapbox.search.tests_support.TestThreadExecutorService
import com.mapbox.search.tests_support.catchThrowable
import com.mapbox.search.tests_support.createTestCoreSearchResult
import com.mapbox.search.tests_support.createTestCoreSuggestAction
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

/**
 * Contains only forward-geocoding related functionality tests.
 * See [CategorySearchTest], [ReverseGeocodingSearchTest] for more tests.
 */
@Suppress("LargeClass")
internal class SearchEngineTest {

    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var dataProviderResolver: DataProviderResolver
    private lateinit var httpErrorsCache: HttpErrorsCache
    private lateinit var historyService: HistoryService
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var executor: Executor
    private lateinit var engineExecutorService: ExecutorService
    private lateinit var requestContextProvider: SearchRequestContextProvider
    private lateinit var indexableDataProvidersRegistry: IndexableDataProvidersRegistry

    private lateinit var searchEngine: SearchEngine

    @BeforeEach
    fun setUp() {
        coreEngine = mockk(relaxed = true)
        httpErrorsCache = mockk()
        historyService = mockk(relaxed = true)

        dataProviderResolver = mockk()
        every { dataProviderResolver.getRecordsLayer(any()) } returns null

        searchResultFactory = spyk(SearchResultFactory(dataProviderResolver))

        val slotSuggestionCallback = slot<(Result<SearchSuggestion>) -> Unit>()
        every { searchResultFactory.createSearchSuggestionAsync(any(), any(), ApiType.SBS, any(), any(), capture(slotSuggestionCallback)) }.answers {
            slotSuggestionCallback.captured(Result.success(TEST_GEOCODING_SEARCH_SUGGESTION))
            CompletedAsyncOperationTask
        }

        executor = spyk(TestExecutor())
        engineExecutorService = spyk(TestThreadExecutorService())

        requestContextProvider = mockk()
        every { requestContextProvider.provide(ApiType.SBS) } returns TEST_SEARCH_REQUEST_CONTEXT

        indexableDataProvidersRegistry = mockk()

        searchEngine = SearchEngineImpl(
            apiType = ApiType.SBS,
            coreEngine = coreEngine,
            httpErrorsCache = httpErrorsCache,
            historyService = historyService,
            requestContextProvider = requestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = engineExecutorService,
            indexableDataProvidersRegistry = indexableDataProvidersRegistry,
        )
    }

    @TestFactory
    fun `Check initial successful search call`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()

            every { coreEngine.search(any(), any(), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Initial search called") {
                val callback = mockk<SearchSuggestionsCallback>(relaxed = true)

                val searchRequestTask = searchEngine.search(
                    query = TEST_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                ) as SearchRequestTaskImpl<*>

                Then("SearchRequestTask released reference to callback", true, searchRequestTask.callbackDelegate == null)
                // TODO(#224): test isExecuted/isCanceled properties
                // Then("SearchRequestTask is executed", true, searchRequestTask.isExecuted)
                // Then("SearchRequestTask is not cancelled", false, searchRequestTask.isCancelled)

                Verify("Operation is scheduled on engine thread") {
                    engineExecutorService.submit(any())
                }

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("CoreSearchEngine.search() called") {
                    coreEngine.search(
                        eq(TEST_QUERY),
                        eq(emptyList()),
                        eq(TEST_SEARCH_OPTIONS.mapToCore()),
                        slotSearchCallback.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onSuggestions(
                        listOf(TEST_GEOCODING_SEARCH_SUGGESTION),
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS.mapToPlatform(
                                TEST_SEARCH_REQUEST_CONTEXT.copy(responseUuid = TEST_RESPONSE_UUID)
                            ),
                            TEST_SUCCESSFUL_CORE_RESPONSE,
                            isReproducible = true,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check initial error search call`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()
            val slotSearchOptions = slot<CoreSearchOptions>()

            every { coreEngine.search(any(), any(), capture(slotSearchOptions), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(TEST_ERROR_CORE_RESPONSE)
            }

            val errorCause = IOException()
            every { httpErrorsCache.getAndRemove(TEST_REQUEST_ID) } returns errorCause

            When("Initial search called") {
                val callback = mockk<SearchSuggestionsCallback>(relaxed = true)

                val searchRequestTask = searchEngine.search(
                    query = TEST_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                ) as SearchRequestTaskImpl<*>

                Then("SearchRequestTask released reference to callback", true, searchRequestTask.callbackDelegate == null)
                // TODO(#224): test isExecuted/isCanceled properties
                // Then("SearchRequestTask is executed", true, searchRequestTask.isExecuted)
                // Then("SearchRequestTask is not cancelled", false, searchRequestTask.isCancelled)

                Verify("Operation is scheduled on engine thread") {
                    engineExecutorService.submit(any())
                }

                Verify("CoreSearchEngine.search() called") {
                    coreEngine.search(
                        eq(TEST_QUERY),
                        eq(emptyList()),
                        slotSearchOptions.captured,
                        slotSearchCallback.captured
                    )
                }

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("Error cause retrieved from errors cache") {
                    httpErrorsCache.getAndRemove(TEST_REQUEST_ID)
                }

                Verify("Error passed to callback") {
                    callback.onError(errorCause)
                }
            }
        }
    }

    @TestFactory
    fun `Check initial internal error search call`() = TestCase {
        Given("SearchEngine with failing SearchResultFactory") {
            val exception = RuntimeException("Test error")
            every { searchResultFactory.createSearchSuggestionAsync(any(), any(), any(), any(), any(), any()) } throws exception
            val slotSearchCallback = slot<CoreSearchCallback>()
            every {
                coreEngine.search(any(), any(), any(), capture(slotSearchCallback))
            } answers {
                slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Initial search called") {
                val callback = mockk<SearchSuggestionsCallback>(relaxed = true)
                val slotCallbackError = slot<Exception>()
                every { callback.onError(capture(slotCallbackError)) } returns Unit

                searchEngine.search(
                    query = TEST_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                )

                Then("Exception from SearchResultFactory should be forwarded to callback", slotCallbackError.captured, exception)
            }
        }
    }

//    TODO(#224): uncomment when isExecuted/isCanceled properties are available
//    @TestFactory
//    fun `Check consecutive search calls`() = TestCase {
//        Given("SearchEngine with mocked dependencies") {
//            val slotSearchCallback = slot<CoreSearchCallback>()
//            val callback = mockk<SearchSuggestionsCallback>(relaxed = true)
//
//            every { coreEngine.search(any(), any(), any(), capture(slotSearchCallback)) } returns Unit
//
//            var searchRequestTask1: SearchRequestTaskImpl<*>? = null
//            When("Search function called for the first time") {
//                searchRequestTask1 =
//                    (searchEngine.search(TEST_QUERY, TEST_SEARCH_OPTIONS, callback) as SearchRequestTaskImpl<*>?)
//
//                Then("First task is not executed", false, searchRequestTask1?.isExecuted)
//                Then("First task is not cancelled", false, searchRequestTask1?.isCancelled)
//                Then("First task keeps original callback", true, searchRequestTask1?.callbackDelegate != null)
//            }
//
//            var searchRequestTask2: SearchRequestTaskImpl<*>? = null
//            When("Search function called for the second time") {
//                searchRequestTask2 =
//                    (searchEngine.search(TEST_QUERY, TEST_SEARCH_OPTIONS, callback) as SearchRequestTaskImpl<*>?)
//
//                Then("First task is not executed", false, searchRequestTask1?.isExecuted)
//                Then("First task is cancelled", true, searchRequestTask1?.isCancelled)
//                Then(
//                    "First task released reference to original callback",
//                    true,
//                    searchRequestTask1?.callbackDelegate == null
//                )
//
//                Then("Second task is not executed", false, searchRequestTask2?.isExecuted)
//                Then("Second task is not cancelled", false, searchRequestTask2?.isCancelled)
//                Then("Second task keeps original callback", true, searchRequestTask2?.callbackDelegate != null)
//            }
//
//            When("Second search request completes") {
//                slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
//
//                Then("Second task is executed", true, searchRequestTask2?.isExecuted)
//                Then("Second task is not cancelled", false, searchRequestTask2?.isCancelled)
//                Then(
//                    "Second task released reference to original callback",
//                    true,
//                    searchRequestTask2?.callbackDelegate == null
//                )
//            }
//        }
//    }

    @TestFactory
    fun `Check request marked as executed when internal error happens`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()

            every { coreEngine.search(any(), any(), any(), capture(slotSearchCallback)) } returns Unit

            val callback = mockk<SearchSuggestionsCallback>(relaxed = true)
            every { callback.onSuggestions(any(), any()) } throws IllegalStateException()

            var task: SearchRequestTaskImpl<*>? = null
            When("Search function called for the first time") {
                val throwable = catchThrowable<IllegalStateException> {
                    task = searchEngine.search(
                        query = TEST_QUERY,
                        options = TEST_SEARCH_OPTIONS,
                        executor = executor,
                        callback = callback
                    ) as SearchRequestTaskImpl<*>?

                    slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
                }

                Then("Task failed with IllegalStateException", true, throwable != null)

                // TODO(#224): test isExecuted/isCanceled properties
                // Then("Task is executed", true, task?.isExecuted)
                // Then("Task is not cancelled", false, task?.isCancelled)
                Then("Task released reference to original callback", true, task?.callbackDelegate == null)
            }
        }
    }

    @TestFactory
    fun `Check successful search selection with geocoding suggestion`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotRequestOptions = slot<CoreRequestOptions>()
            val slotSearchResult = slot<CoreSearchResult>()
            every { coreEngine.onSelected(capture(slotRequestOptions), capture(slotSearchResult)) } returns Unit

            val historyServiceCompletionCallbackSlot = slot<CompletionCallback<Boolean>>()
            every { historyService.addToHistoryIfNeeded(any(), any(), capture(historyServiceCompletionCallbackSlot)) } answers {
                historyServiceCompletionCallbackSlot.captured.onComplete(true)
                CompletedAsyncOperationTask
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)

                val searchRequestTask = searchEngine.select(
                    suggestion = TEST_GEOCODING_SEARCH_SUGGESTION,
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback
                ) as SearchRequestTaskImpl<*>

                Then("SearchRequestTask released reference to callback", true, searchRequestTask.callbackDelegate == null)
                // TODO(#224): test isExecuted/isCanceled properties
                // Then("SearchRequestTask is executed", true, searchRequestTask.isExecuted)
                // Then("SearchRequestTask is not cancelled", false, searchRequestTask.isCancelled)

                Verify("Operation is scheduled on engine thread") {
                    engineExecutorService.submit(any())
                }

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("CoreSearchEngine.onSelected() called") {
                    coreEngine.onSelected(slotRequestOptions.captured, slotSearchResult.captured)
                }

                Verify("Suggestion added to history") {
                    historyService.addToHistoryIfNeeded(
                        TEST_SEARCH_RESULT, any(),
                        historyServiceCompletionCallbackSlot.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onResult(
                        TEST_GEOCODING_SEARCH_SUGGESTION,
                        TEST_SEARCH_RESULT,
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT),
                            null,
                            isReproducible = false,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check successful search selection with SBS suggestion`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSelectRequestOptions = slot<CoreRequestOptions>()
            val slotSelectSearchResult = slot<CoreSearchResult>()
            every { coreEngine.onSelected(capture(slotSelectRequestOptions), capture(slotSelectSearchResult)) } returns Unit

            val slotRetrieveSearchCallback = slot<CoreSearchCallback>()
            every {
                coreEngine.retrieve(any(), any(), capture(slotRetrieveSearchCallback))
            } answers {
                slotRetrieveSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            val historyServiceCompletionCallbackSlot = slot<CompletionCallback<Boolean>>()
            every { historyService.addToHistoryIfNeeded(any(), any(), capture(historyServiceCompletionCallbackSlot)) } answers {
                historyServiceCompletionCallbackSlot.captured.onComplete(true)
                CompletedAsyncOperationTask
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)

                val searchRequestTask = searchEngine.select(
                    suggestion = TEST_SBS_SERVER_SEARCH_SUGGESTION,
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback,
                ) as SearchRequestTaskImpl<*>

                Verify("CoreSearchEngine.retrieve() Called") {
                    coreEngine.retrieve(any(), any(), any())
                }

                Then("SearchRequestTask released reference to callback", null, searchRequestTask.callbackDelegate)
                // TODO(#224): test isExecuted/isCanceled properties
                // Then("SearchRequestTask is executed", true, searchRequestTask.isExecuted)
                // Then("SearchRequestTask is not cancelled", false, searchRequestTask.isCancelled)

                Verify("Operation is scheduled on engine thread") {
                    engineExecutorService.submit(any())
                }

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                val expectedSearchRequestContext = requestContextProvider.provide(ApiType.SBS)
                    .copy(responseUuid = TEST_SUCCESSFUL_CORE_RESPONSE.responseUUID)

                val expectedRequestOptions = TEST_SUCCESSFUL_CORE_RESPONSE.request.mapToPlatform(
                    searchRequestContext = expectedSearchRequestContext
                )

                val selectedResult = TEST_SUCCESSFUL_CORE_RESPONSE.results.first()

                val expectedResult = searchResultFactory.createSearchResult(
                    selectedResult.mapToPlatform(),
                    expectedRequestOptions
                )!!

                Verify("CoreSearchEngine.onSelected() called") {
                    coreEngine.onSelected(TEST_SUCCESSFUL_CORE_RESPONSE.request, selectedResult)
                }

                Verify("Suggestion added to history") {
                    historyService.addToHistoryIfNeeded(
                        expectedResult,
                        any(),
                        historyServiceCompletionCallbackSlot.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onResult(
                        TEST_SBS_SERVER_SEARCH_SUGGESTION,
                        expectedResult,
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS.mapToPlatform(
                                TEST_SEARCH_REQUEST_CONTEXT.copy(responseUuid = TEST_RESPONSE_UUID)
                            ),
                            null,
                            isReproducible = false,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check successful search selection with data layer suggestion`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotRequestOptions = slot<CoreRequestOptions>()
            val slotSearchResult = slot<CoreSearchResult>()
            every { coreEngine.onSelected(capture(slotRequestOptions), capture(slotSearchResult)) } returns Unit

            val indexableRecordProviderCompletionCallbackSlot = slot<CompletionCallback<IndexableRecord?>>()
            val indexableRecordProvider = mockk<IndexableDataProvider<*>>()
            every { indexableRecordProvider.get(TEST_USER_LAYER_RECORD_ID, capture(indexableRecordProviderCompletionCallbackSlot)) } answers {
                indexableRecordProviderCompletionCallbackSlot.captured.onComplete(TEST_FAVORITE_RECORD)
                CompletedAsyncOperationTask
            }

            every { dataProviderResolver.getRecordsLayer(FavoritesDataProvider.PROVIDER_NAME) } returns indexableRecordProvider

            val historyServiceCompletionCallbackSlot = slot<CompletionCallback<Boolean>>()
            every { historyService.addToHistoryIfNeeded(any(), any(), capture(historyServiceCompletionCallbackSlot)) } answers {
                historyServiceCompletionCallbackSlot.captured.onComplete(true)
                CompletedAsyncOperationTask
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)

                val searchRequestTask = searchEngine.select(
                    suggestion = TEST_USER_RECORD_SEARCH_SUGGESTION,
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback
                ) as SearchRequestTaskImpl<*>

                Then("SearchRequestTask released reference to callback", true, searchRequestTask.callbackDelegate == null)
                // TODO(#224): test isExecuted/isCanceled properties
                // Then("SearchRequestTask is executed", true, searchRequestTask.isExecuted)
                // Then("SearchRequestTask is not cancelled", false, searchRequestTask.isCancelled)

                Verify("Operation is scheduled on engine thread") {
                    engineExecutorService.submit(any())
                }

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("CoreSearchEngine.onSelected() called") {
                    coreEngine.onSelected(slotRequestOptions.captured, slotSearchResult.captured)
                }

                Verify("Suggestion added to history") {
                    historyService.addToHistoryIfNeeded(
                        TEST_FAVORITE_RECORD_SEARCH_RESULT,
                        any(),
                        historyServiceCompletionCallbackSlot.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onResult(
                        TEST_USER_RECORD_SEARCH_SUGGESTION,
                        TEST_FAVORITE_RECORD_SEARCH_RESULT,
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT),
                            null,
                            isReproducible = false,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check search selection with initial internal error`() = TestCase {
        Given("SearchEngine with failing SearchResultFactory") {
            val exception = RuntimeException("Test error")
            every { searchResultFactory.createSearchResult(any(), any()) } throws exception

            every { coreEngine.onSelected(any(), any()) } returns Unit

            val slotRetrieveSearchCallback = slot<CoreSearchCallback>()
            every {
                coreEngine.retrieve(any(), any(), capture(slotRetrieveSearchCallback))
            } answers {
                slotRetrieveSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)
                val slotCallbackError = slot<Exception>()
                every { callback.onError(capture(slotCallbackError)) } returns Unit

                searchEngine.select(
                    suggestion = TEST_SBS_SERVER_SEARCH_SUGGESTION,
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback
                ) as SearchRequestTaskImpl<*>

                Then("Exception from SearchResultFactory should be forwarded to callback", slotCallbackError.captured, exception)
            }
        }
    }

    private companion object {

        const val TEST_QUERY = "Minsk"
        val TEST_SEARCH_OPTIONS = SearchOptions()
        const val TEST_REQUEST_ID = 123
        const val TEST_RESPONSE_UUID = "UUID test"
        const val TEST_RESPONSE_UUID_2 = "UUID test 2"
        const val TEST_DESCRIPTION_TEXT = "Test description text"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.0, 11.0)
        val TEST_SEARCH_ADDRESS = SearchAddress(null, null, null, null, null, null, null, null, null)
        val TEST_SEARCH_REQUEST_CONTEXT = SearchRequestContext(ApiType.SBS, responseUuid = TEST_RESPONSE_UUID_2)

        val TEST_REQUEST_OPTIONS = createTestRequestOptions(
            query = TEST_QUERY,
            options = SearchOptions(
                proximity = TEST_USER_LOCATION
            ),
        ).mapToCore()

        val TEST_CORE_SEARCH_RESULT = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(ResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            categories = emptyList(),
            descriptionAddress = "Test result description"
        )

        val TEST_CORE_SEARCH_SUGGESTION = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(ResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            categories = emptyList(),
            descriptionAddress = "Test result description",
        )

        val TEST_SUCCESSFUL_CORE_RESPONSE = CoreSearchResponse(
            true,
            200,
            "ok",
            TEST_REQUEST_ID,
            TEST_REQUEST_OPTIONS,
            listOf(TEST_CORE_SEARCH_RESULT),
            TEST_RESPONSE_UUID
        )

        val TEST_ERROR_CORE_RESPONSE = CoreSearchResponse(
            false,
            401,
            "Auth failed",
            TEST_REQUEST_ID,
            TEST_REQUEST_OPTIONS,
            emptyList(),
            TEST_RESPONSE_UUID
        )

        val TEST_SEARCH_RESULT = ServerSearchResultImpl(
            listOf(SearchResultType.ADDRESS),
            TEST_CORE_SEARCH_RESULT.mapToPlatform(),
            TEST_REQUEST_OPTIONS.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT)
        )

        val TEST_GEOCODING_SEARCH_SUGGESTION = GeocodingCompatSearchSuggestion(
            TEST_CORE_SEARCH_SUGGESTION.mapToPlatform(),
            TEST_REQUEST_OPTIONS.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT)
        )

        val TEST_SBS_SERVER_SEARCH_SUGGESTION = ServerSearchSuggestion(
            TEST_CORE_SEARCH_SUGGESTION.mapToPlatform().copy(
                types = listOf(OriginalResultType.POI),
                action = createTestCoreSuggestAction().mapToPlatform()
            ),
            TEST_REQUEST_OPTIONS.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT)
        )

        const val TEST_USER_LAYER_RECORD_ID = "test user layer record id"

        val TEST_USER_RECORD_SEARCH_RESULT = createTestCoreSearchResult(
            id = TEST_USER_LAYER_RECORD_ID,
            types = listOf(ResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            categories = emptyList(),
            layerId = FavoritesDataProvider.PROVIDER_NAME,
            userRecordId = TEST_USER_LAYER_RECORD_ID
        )

        val TEST_FAVORITE_RECORD = FavoriteRecord(
            id = TEST_USER_LAYER_RECORD_ID,
            name = "Test favorite",
            coordinate = TEST_USER_LOCATION,
            descriptionText = TEST_DESCRIPTION_TEXT,
            address = TEST_SEARCH_ADDRESS.mapToPlatform(),
            type = SearchResultType.ADDRESS,
            makiIcon = null,
            categories = emptyList(),
            routablePoints = null,
            metadata = null
        )

        val TEST_USER_RECORD_SEARCH_SUGGESTION = IndexableRecordSearchSuggestion(
            TEST_FAVORITE_RECORD,
            originalSearchResult = TEST_USER_RECORD_SEARCH_RESULT.mapToPlatform().copy(
                types = listOf(OriginalResultType.USER_RECORD)
            ),
            requestOptions = TEST_REQUEST_OPTIONS.mapToPlatform(TEST_SEARCH_REQUEST_CONTEXT)
        )

        val TEST_FAVORITE_RECORD_SEARCH_RESULT = IndexableRecordSearchResultImpl(
            record = TEST_FAVORITE_RECORD,
            originalSearchResult = TEST_USER_RECORD_SEARCH_SUGGESTION.originalSearchResult,
            requestOptions = TEST_USER_RECORD_SEARCH_SUGGESTION.requestOptions
        )

        @Suppress("DEPRECATION", "JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
            every { reportError(any()) } returns Unit
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            unmockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
