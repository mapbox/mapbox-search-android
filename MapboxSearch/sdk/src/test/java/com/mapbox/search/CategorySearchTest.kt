package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.logger.reinitializeLogImpl
import com.mapbox.search.common.logger.resetLogImpl
import com.mapbox.search.core.CoreSearchCallback
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.CoreSearchOptions
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
import com.mapbox.search.tests_support.createTestCoreSearchResponseCancelled
import com.mapbox.search.tests_support.createTestCoreSearchResponseHttpError
import com.mapbox.search.tests_support.createTestCoreSearchResponseSuccess
import com.mapbox.search.tests_support.createTestCoreSearchResult
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
import java.util.concurrent.Executor

/**
 * Contains only forward-geocoding related functionality tests.
 * See [SearchEngineTest], [ReverseGeocodingSearchTest] for more tests.
 */
internal class CategorySearchTest {

    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var dataProviderResolver: DataProviderResolver
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var executor: Executor
    private lateinit var requestContextProvider: SearchRequestContextProvider

    private lateinit var searchEngine: SearchEngine

    @BeforeEach
    fun setUp() {
        coreEngine = mockk(relaxed = true)
        dataProviderResolver = mockk()
        searchResultFactory = spyk(SearchResultFactory(dataProviderResolver))
        executor = spyk(TestExecutor())
        requestContextProvider = mockk()

        every { requestContextProvider.provide(ApiType.GEOCODING) } returns TEST_SEARCH_REQUEST_CONTEXT

        searchEngine = SearchEngineImpl(
            apiType = ApiType.GEOCODING,
            coreEngine = coreEngine,
            historyService = mockk(),
            requestContextProvider = requestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = TestThreadExecutorService(),
            indexableDataProvidersRegistry = mockk(),
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
                Then("SearchRequestTask is executed", true, searchRequestTask.isDone)
                Then("SearchRequestTask is not cancelled", false, searchRequestTask.isCancelled)

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
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS,
                            TEST_SUCCESSFUL_CORE_RESPONSE.mapToPlatform(),
                            isReproducible = true
                        )
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

            When("Initial search called") {
                val callback = mockk<SearchCallback>(relaxed = true)

                val searchRequestTask = searchEngine.search(
                    categoryName = TEST_CATEGORIES_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                ) as SearchRequestTaskImpl<*>

                Then("SearchRequestTask released reference to callback", true, searchRequestTask.callbackDelegate == null)
                Then("SearchRequestTask is executed", true, searchRequestTask.isDone)
                Then("SearchRequestTask is not cancelled", false, searchRequestTask.isCancelled)

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

                Verify("Error passed to callback") {
                    callback.onError(
                        SearchRequestException(
                            message = TEST_ERROR_CORE_RESPONSE_MESSAGE,
                            code = TEST_ERROR_CORE_RESPONSE_HTTP_CODE
                        )
                    )
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

    @TestFactory
    fun `Check search call cancellation`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            every { searchResultFactory.createSearchResult(any(), any()) } returns TEST_SEARCH_RESULT

            val cancellationReason = "Request cancelled"

            val slotSearchCallback = slot<CoreSearchCallback>()
            every { coreEngine.search(eq(""), eq(listOf(TEST_CATEGORIES_QUERY)), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(createTestCoreSearchResponseCancelled(cancellationReason))
            }

            When("Search request cancelled by the Search SDK") {
                val callback = mockk<SearchCallback>(relaxed = true)

                val task = (searchEngine.search(TEST_CATEGORIES_QUERY, TEST_SEARCH_OPTIONS, callback) as? SearchRequestTaskImpl<*>)

                Then("Task is not executed", false, task?.isDone)
                Then("Task is cancelled", true, task?.isCancelled)
                Then(
                    "Task released reference to original callback",
                    true,
                    task?.callbackDelegate == null
                )

                VerifyOnce("Callback called with cancellation error") {
                    callback.onError(eq(SearchCancellationException(cancellationReason)))
                }
            }
        }
    }

    private companion object {

        const val TEST_CATEGORIES_QUERY = "cafe"

        val TEST_SEARCH_OPTIONS = CategorySearchOptions()

        const val TEST_RESPONSE_UUID = "UUID test"

        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.0, 11.0)
        val TEST_SEARCH_ADDRESS = SearchAddress(null, null, null, null, null, null, null, null, null)
        val TEST_SEARCH_REQUEST_CONTEXT = SearchRequestContext(apiType = ApiType.GEOCODING, responseUuid = TEST_RESPONSE_UUID)

        val TEST_CORE_SEARCH_RESULT = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(ResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            routablePoints = emptyList(),
            categories = emptyList(),
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

        val TEST_CORE_SEARCH_SUGGESTION = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(ResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            routablePoints = emptyList(),
            categories = emptyList(),
        )

        val TEST_SEARCH_SUGGESTION = GeocodingCompatSearchSuggestion(
            originalSearchResult = TEST_CORE_SEARCH_SUGGESTION.mapToPlatform(),
            requestOptions = TEST_REQUEST_OPTIONS
        )

        val TEST_SUCCESSFUL_CORE_RESPONSE = createTestCoreSearchResponseSuccess(
            TEST_REQUEST_OPTIONS.mapToCore(),
            listOf(TEST_CORE_SEARCH_RESULT),
            TEST_RESPONSE_UUID
        )

        const val TEST_ERROR_CORE_RESPONSE_HTTP_CODE = 401

        const val TEST_ERROR_CORE_RESPONSE_MESSAGE = "Auth failed"

        val TEST_ERROR_CORE_RESPONSE = createTestCoreSearchResponseHttpError(
            TEST_ERROR_CORE_RESPONSE_HTTP_CODE,
            TEST_ERROR_CORE_RESPONSE_MESSAGE,
            TEST_REQUEST_OPTIONS.mapToCore(),
            TEST_RESPONSE_UUID
        )

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
