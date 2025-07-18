package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreSearchCallback
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.result.BaseGeocodingCompatSearchSuggestion
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.RestrictedMapboxSearchAPI
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.tests.TestExecutor
import com.mapbox.search.common.tests.TestThreadExecutorService
import com.mapbox.search.common.tests.createTestCoreSearchResponseHttpError
import com.mapbox.search.common.tests.createTestCoreSearchResponseSuccess
import com.mapbox.search.common.tests.createTestCoreSearchResult
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.internal.bindgen.SearchAddress
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import com.mapbox.search.result.SearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executor

@OptIn(RestrictedMapboxSearchAPI::class)
internal class BrandSearchTest {

    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var activityReporter: UserActivityReporterInterface
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var executor: Executor
    private lateinit var requestContextProvider: SearchRequestContextProvider

    private lateinit var searchEngine: SearchEngine

    @BeforeEach
    fun setUp() {
        coreEngine = mockk(relaxed = true)
        activityReporter = mockk(relaxed = true)
        searchResultFactory = spyk(SearchResultFactory(mockk()))
        executor = spyk(TestExecutor())
        requestContextProvider = mockk()

        every { requestContextProvider.provide(CoreApiType.SEARCH_BOX) } returns TEST_SEARCH_REQUEST_CONTEXT

        searchEngine = mockSearchEngine(ApiType.SEARCH_BOX)
    }

    @Test
    fun `test initial successful search call`() {
        val slotSuggestionCallback = slot<(Result<BaseSearchSuggestion>) -> Unit>()
        every {
            searchResultFactory.createSearchSuggestionAsync(
                any(), any(), any(), any(), capture(slotSuggestionCallback)
            )
        }.answers {
            slotSuggestionCallback.captured(Result.success(TEST_SEARCH_SUGGESTION))
            AsyncOperationTaskImpl.COMPLETED
        }

        val slotSearchCallback = slot<CoreSearchCallback>()
        every { coreEngine.brandSearch(any(), capture(slotSearchCallback)) }.answers {
            slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            TEST_REQUEST_ID
        }

        val callback = mockk<SearchCallback>(relaxed = true)

        val task = searchEngine.brandSearch(
            brandName = TEST_BRAND_QUERY,
            options = TEST_SEARCH_OPTIONS,
            executor = executor,
            callback = callback
        )

        assertTrue(task.isDone)

        verify(exactly = 1) {
            executor.execute(any())
        }

        verify(exactly = 1) {
            coreEngine.brandSearch(
                eq(TEST_SEARCH_OPTIONS.mapToCoreBrandOptions(TEST_BRAND_QUERY)),
                slotSearchCallback.captured
            )
        }

        verify(exactly = 1) {
            callback.onResults(
                listOf(SearchResult(TEST_SEARCH_RESULT)),
                ResponseInfo(
                    TEST_REQUEST_OPTIONS,
                    TEST_SUCCESSFUL_CORE_RESPONSE.mapToBase(),
                    isReproducible = true
                )
            )
        }

        verify(exactly = 0) {
            coreEngine.cancel(any())
        }

        verify(exactly = 1) {
            activityReporter.reportActivity(eq("search-engine-brand-search"))
        }
    }

    @Test
    fun `test initial error search call`() {
        val slotSearchCallback = slot<CoreSearchCallback>()
        every { coreEngine.brandSearch(any(), capture(slotSearchCallback)) }.answers {
            slotSearchCallback.captured.run(TEST_ERROR_CORE_RESPONSE)
            TEST_REQUEST_ID
        }

        val callback = mockk<SearchCallback>(relaxed = true)

        val task = searchEngine.brandSearch(
            brandName = TEST_BRAND_QUERY,
            options = TEST_SEARCH_OPTIONS,
            executor = executor,
            callback = callback
        )

        assertTrue(task.isDone)

        verify(exactly = 1) {
            executor.execute(any())
        }

        verify(exactly = 1) {
            coreEngine.brandSearch(
                eq(TEST_SEARCH_OPTIONS.mapToCoreBrandOptions(TEST_BRAND_QUERY)),
                slotSearchCallback.captured
            )
        }

        verify(exactly = 1) {
            callback.onError(
                SearchRequestException(
                    message = TEST_ERROR_CORE_RESPONSE_MESSAGE,
                    code = TEST_ERROR_CORE_RESPONSE_HTTP_CODE
                )
            )
        }

        verify(exactly = 0) {
            coreEngine.cancel(any())
        }

        verify(exactly = 1) {
            activityReporter.reportActivity(eq("search-engine-brand-search"))
        }
    }

    @Test
    fun `test search call cancellation initiated by user`() {
        every { coreEngine.brandSearch(any(), any()) } answers {
            TEST_REQUEST_ID
        }

        val callback = mockk<SearchCallback>(relaxed = true)

        val task = searchEngine.brandSearch(
            brandName = TEST_BRAND_QUERY,
            options = TEST_SEARCH_OPTIONS,
            executor = executor,
            callback = callback
        )

        task.cancel()

        assertTrue(task.isCancelled)

        verify(exactly = 0) {
            callback.onResults(any(), any())
            callback.onError(any())
        }

        verify(exactly = 1) {
            coreEngine.cancel(TEST_REQUEST_ID)
        }

        verify(exactly = 1) {
            activityReporter.reportActivity(eq("search-engine-brand-search"))
        }
    }

    @Test
    fun `test unsupported api type`() {
        searchEngine = mockSearchEngine(apiType = ApiType.GEOCODING)

        val slotErrorCallback = slot<Exception>()
        val callback = mockk<SearchCallback>(relaxed = true)
        every { callback.onError(capture(slotErrorCallback)) } returns Unit

        val task = searchEngine.brandSearch(
            brandName = TEST_BRAND_QUERY,
            options = TEST_SEARCH_OPTIONS,
            executor = executor,
            callback = callback
        )

        assertTrue(task.isDone)

        verify(exactly = 1) {
            executor.execute(any())
        }

        verify(exactly = 0) {
            coreEngine.brandSearch(any(), any())
        }

        verify(exactly = 1) {
            callback.onError(slotErrorCallback.captured)
        }

        assertTrue(slotErrorCallback.captured is UnsupportedOperationException)
        assertEquals("Supported only for the SEARCH_BOX api type", slotErrorCallback.captured.message)

        verify(exactly = 0) {
            coreEngine.cancel(any())
        }

        verify(exactly = 1) {
            activityReporter.reportActivity(eq("search-engine-brand-search"))
        }
    }

    private fun mockSearchEngine(apiType: ApiType): SearchEngine {
        return SearchEngineImpl(
            apiType = apiType,
            settings = mockk(),
            analyticsService = mockk(relaxed = true),
            coreEngine = coreEngine,
            activityReporter = activityReporter,
            historyService = mockk(),
            requestContextProvider = requestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = TestThreadExecutorService(),
            indexableDataProvidersRegistry = mockk(),
        )
    }

    private companion object {

        const val TEST_REQUEST_ID = 1L

        const val TEST_BRAND_QUERY = "test-brand"

        val TEST_SEARCH_OPTIONS = BrandSearchOptions()

        const val TEST_RESPONSE_UUID = "UUID test"

        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.0, 11.0)
        val TEST_SEARCH_ADDRESS = SearchAddress(null, null, null, null, null, null, null, null, null)
        val TEST_SEARCH_REQUEST_CONTEXT = SearchRequestContext(apiType = CoreApiType.SEARCH_BOX, responseUuid = TEST_RESPONSE_UUID)

        val TEST_CORE_SEARCH_RESULT = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(ResultType.POI),
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

        val TEST_CORE_SEARCH_SUGGESTION = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(ResultType.POI),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            routablePoints = emptyList(),
            categories = emptyList(),
        )

        val TEST_SEARCH_RESULT = BaseServerSearchResultImpl(
            types = listOf(BaseSearchResultType.POI),
            rawSearchResult = TEST_CORE_SEARCH_RESULT.mapToBase(),
            requestOptions = TEST_REQUEST_OPTIONS.mapToBase()
        )

        val TEST_SEARCH_SUGGESTION = BaseGeocodingCompatSearchSuggestion(
            rawSearchResult = TEST_CORE_SEARCH_SUGGESTION.mapToBase(),
            requestOptions = TEST_REQUEST_OPTIONS.mapToBase()
        )

        val TEST_SUCCESSFUL_CORE_RESPONSE = createTestCoreSearchResponseSuccess(
            TEST_REQUEST_OPTIONS.mapToBase().core,
            listOf(TEST_CORE_SEARCH_RESULT),
            TEST_RESPONSE_UUID
        )

        const val TEST_ERROR_CORE_RESPONSE_HTTP_CODE = 401

        const val TEST_ERROR_CORE_RESPONSE_MESSAGE = "Auth failed"

        val TEST_ERROR_CORE_RESPONSE = createTestCoreSearchResponseHttpError(
            TEST_ERROR_CORE_RESPONSE_HTTP_CODE,
            TEST_ERROR_CORE_RESPONSE_MESSAGE,
            TEST_REQUEST_OPTIONS.mapToBase().core,
            TEST_RESPONSE_UUID
        )
    }
}
