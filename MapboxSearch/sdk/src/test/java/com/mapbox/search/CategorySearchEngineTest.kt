package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.reportError
import com.mapbox.search.core.CoreSearchCallback
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.CoreSearchOptions
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.core.CoreSearchResult
import com.mapbox.search.core.http.HttpErrorsCache
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.internal.bindgen.SearchAddress
import com.mapbox.search.record.DataProviderResolver
import com.mapbox.search.result.GeocodingCompatSearchSuggestion
import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.tests_support.TestExecutor
import com.mapbox.search.tests_support.TestThreadExecutorService
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

internal class CategorySearchEngineTest {

    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var dataProviderResolver: DataProviderResolver
    private lateinit var httpErrorsCache: HttpErrorsCache
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var executor: Executor
    private lateinit var engineExecutorService: ExecutorService
    private lateinit var requestContextProvider: SearchRequestContextProvider

    private lateinit var searchEngine: CategorySearchEngine

    @BeforeEach
    fun setUp() {
        coreEngine = mockk(relaxed = true)
        dataProviderResolver = mockk()
        httpErrorsCache = mockk()
        searchResultFactory = spyk(SearchResultFactory(dataProviderResolver))
        executor = spyk(TestExecutor())
        engineExecutorService = spyk(TestThreadExecutorService())
        requestContextProvider = mockk()

        every { requestContextProvider.provide(ApiType.GEOCODING) } returns TEST_SEARCH_REQUEST_CONTEXT

        searchEngine = CategorySearchEngineImpl(
            apiType = ApiType.GEOCODING,
            coreEngine = coreEngine,
            httpErrorsCache = httpErrorsCache,
            requestContextProvider = requestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = engineExecutorService,
        )
    }

    @TestFactory
    fun `Check initial successful search call`() = TestCase {
        Given("CategorySearchEngine with mocked dependencies") {

            val slotSuggestionCallback = slot<(Result<SearchSuggestion>) -> Unit>()
            every { searchResultFactory.createSearchSuggestionAsync(any(), any(), any(), any(), any(), capture(slotSuggestionCallback)) }.answers {
                slotSuggestionCallback.captured(Result.success(TEST_SEARCH_SUGGESTION))
                CompletedAsyncOperationTask
            }

            val slotSearchCallback = slot<CoreSearchCallback>()
            every { coreEngine.search(any(), any(), any(), capture(slotSearchCallback)) }.answers {
                slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Initial search called") {
                val callback = mockk<SearchCallback>(relaxed = true)

                val searchRequestTask = searchEngine.search(
                    categoryName = TEST_CATEGORIES_QUERY,
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
                        eq(""),
                        eq(listOf(TEST_CATEGORIES_QUERY)),
                        eq(TEST_SEARCH_OPTIONS.mapToCoreCategory()),
                        slotSearchCallback.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onResults(
                        listOf(TEST_SEARCH_RESULT),
                        ResponseInfo(TEST_REQUEST_OPTIONS, TEST_SUCCESSFUL_CORE_RESPONSE, isReproducible = true)
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check initial error search call`() = TestCase {
        Given("CategorySearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()
            val slotSearchOptions = slot<CoreSearchOptions>()

            every { coreEngine.search(any(), any(), capture(slotSearchOptions), capture(slotSearchCallback)) }.answers {
                slotSearchCallback.captured.run(TEST_ERROR_CORE_RESPONSE)
            }

            val errorCause = IOException()
            every { httpErrorsCache.getAndRemove(TEST_REQUEST_ID) } returns errorCause

            When("Initial search called") {
                val callback = mockk<SearchCallback>(relaxed = true)

                val searchRequestTask = searchEngine.search(
                    categoryName = TEST_CATEGORIES_QUERY,
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
                        eq(""),
                        eq(listOf(TEST_CATEGORIES_QUERY)),
                        slotSearchOptions.captured,
                        slotSearchCallback.captured
                    )
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
        Given("CategorySearchEngine with erroneous core response") {
            val exception = RuntimeException("Test error")
            every { searchResultFactory.createSearchSuggestionAsync(any(), any(), any(), any(), any(), any()) } throws exception

            val slotSearchCallback = slot<CoreSearchCallback>()

            every {
                coreEngine.search(any(), any(), any(), capture(slotSearchCallback))
            } answers {
                val spyResponse = spyk(TEST_SUCCESSFUL_CORE_RESPONSE)
                every { spyResponse.results } throws exception
                every { spyResponse.request } throws exception
                slotSearchCallback.captured.run(spyResponse)
            }

            When("Initial search called") {
                val callback = mockk<SearchCallback>(relaxed = true)
                val slotCalbackError = slot<Exception>()
                every { callback.onError(capture(slotCalbackError)) } returns Unit

                searchEngine.search(
                    categoryName = TEST_CATEGORIES_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                )

                Then(
                    "Exception from core response should be forwarded to callback",
                    slotCalbackError.captured,
                    exception
                )
            }
        }
    }

//    TODO(#224): uncomment when isExecuted/isCanceled properties are available
//    @TestFactory
//    fun `Check consecutive search calls`() = TestCase {
//        Given("CategorySearchEngine with mocked dependencies") {
//            val slotSearchCallback = slot<CoreSearchCallback>()
//            val listener = mockk<SearchCallback>(relaxed = true)
//
//            every { coreEngine.search(any(), any(), any(), capture(slotSearchCallback)) } returns Unit
//
//            every { searchResultFactory.createSearchSuggestion(any(), any()) } returns TEST_SEARCH_RESULT
//
//            var searchRequestTask1: SearchRequestTaskImpl<*>? = null
//            When("Search function called for the first time") {
//                searchRequestTask1 = (searchEngine.search(TEST_CATEGORIES_QUERY, TEST_SEARCH_OPTIONS, listener) as SearchRequestTaskImpl<*>?)
//
//                Then("First task is not executed", false, searchRequestTask1?.isExecuted)
//                Then("First task is not cancelled", false, searchRequestTask1?.isCancelled)
//                Then("First task keeps original listener", true, searchRequestTask1?.callbackDelegate != null)
//            }
//
//            var searchRequestTask2: SearchRequestTaskImpl<*>? = null
//            When("Search function called for the second time") {
//                searchRequestTask2 = (searchEngine.search(TEST_CATEGORIES_QUERY, TEST_SEARCH_OPTIONS, listener) as SearchRequestTaskImpl<*>?)
//
//                Then("First task is not executed", false, searchRequestTask1?.isExecuted)
//                Then("First task is cancelled", true, searchRequestTask1?.isCancelled)
//                Then(
//                    "First task released reference to original listener",
//                    true,
//                    searchRequestTask1?.callbackDelegate == null
//                )
//
//                Then("Second task is not executed", false, searchRequestTask2?.isExecuted)
//                Then("Second task is not cancelled", false, searchRequestTask2?.isCancelled)
//                Then("Second task keeps original listener", true, searchRequestTask2?.callbackDelegate != null)
//            }
//
//            When("Second search request completes") {
//                slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
//
//                Then("Second task is executed", true, searchRequestTask2?.isExecuted)
//                Then("Second task is not cancelled", false, searchRequestTask2?.isCancelled)
//                Then(
//                    "Second task released reference to original listener",
//                    true,
//                    searchRequestTask2?.callbackDelegate == null
//                )
//            }
//        }
//    }

    private companion object {

        const val TEST_CATEGORIES_QUERY = "cafe"

        val TEST_SEARCH_OPTIONS = CategorySearchOptions()

        const val TEST_REQUEST_ID = 123

        const val TEST_RESPONSE_UUID = "UUID test"

        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.0, 11.0)
        val TEST_SEARCH_ADDRESS = SearchAddress(null, null, null, null, null, null, null, null, null)
        val TEST_SEARCH_REQUEST_CONTEXT = SearchRequestContext(apiType = ApiType.GEOCODING, responseUuid = TEST_RESPONSE_UUID)

        val TEST_CORE_SEARCH_RESULT = CoreSearchResult(
            "test result id",
            listOf(ResultType.ADDRESS),
            listOf("Result name"),
            listOf("Default"),
            listOf(TEST_SEARCH_ADDRESS),
            null,
            123.0,
            null,
            Point.fromLngLat(20.0, 30.0),
            emptyList(),
            emptyList(),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val TEST_REQUEST_OPTIONS = createTestRequestOptions(
            "",
            options = SearchOptions(proximity = TEST_USER_LOCATION),
            proximityRewritten = true,
            requestContext = TEST_SEARCH_REQUEST_CONTEXT
        )

        val TEST_SEARCH_RESULT = ServerSearchResultImpl(
            types = listOf(SearchResultType.ADDRESS),
            originalSearchResult = TEST_CORE_SEARCH_RESULT.mapToPlatform(),
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_CORE_SEARCH_SUGGESTION = CoreSearchResult(
            "test result id",
            listOf(ResultType.ADDRESS),
            listOf("Result name"),
            listOf("Default"),
            listOf(TEST_SEARCH_ADDRESS),
            null,
            123.0,
            null,
            Point.fromLngLat(20.0, 30.0),
            emptyList(),
            emptyList(),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val TEST_SEARCH_SUGGESTION = GeocodingCompatSearchSuggestion(
            originalSearchResult = TEST_CORE_SEARCH_SUGGESTION.mapToPlatform(),
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_SUCCESSFUL_CORE_RESPONSE = CoreSearchResponse(
            true,
            200,
            "ok",
            TEST_REQUEST_ID,
            TEST_REQUEST_OPTIONS.mapToCore(),
            listOf(TEST_CORE_SEARCH_RESULT),
            TEST_RESPONSE_UUID
        )

        val TEST_ERROR_CORE_RESPONSE = CoreSearchResponse(
            false,
            401,
            "Auth failed",
            TEST_REQUEST_ID,
            TEST_REQUEST_OPTIONS.mapToCore(),
            emptyList(),
            TEST_RESPONSE_UUID
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
