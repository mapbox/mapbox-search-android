package com.mapbox.search.discover

import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.TestExecutor
import com.mapbox.search.common.TestThreadExecutorService
import com.mapbox.search.common.createTestCoreSearchOptions
import com.mapbox.search.common.createTestCoreSearchResponseHttpError
import com.mapbox.search.common.createTestCoreSearchResponseSuccess
import com.mapbox.search.common.createTestCoreSearchResult
import com.mapbox.search.internal.bindgen.ApiType
import com.mapbox.search.internal.bindgen.SearchCallback
import com.mapbox.search.internal.bindgen.SearchResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

internal class DiscoverApiSearchEngineTest {

    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var requestContextProvider: SearchRequestContextProvider
    private lateinit var searchResultFactory: SearchResultFactory
    private val callbackExecutor: Executor = TestExecutor()
    private val engineExecutorService: ExecutorService = TestThreadExecutorService()

    private lateinit var engine: DiscoverApiSearchEngine

    @BeforeEach
    fun setUp() {
        coreEngine = mockk(relaxed = true)

        requestContextProvider = mockk()
        every { requestContextProvider.provide(any()) } returns SearchRequestContext(ApiType.SBS)

        searchResultFactory = SearchResultFactory(IndexableRecordResolver.EMPTY)

        engine = DiscoverApiSearchEngine(
            coreEngine,
            requestContextProvider,
            searchResultFactory,
            callbackExecutor,
            engineExecutorService
        )
    }

    @Test
    fun `Check parameters passing to the core engine`() {
        mockEngineResponse(createTestCoreSearchResponseSuccess())

        runBlocking {
            engine.search(TEST_CATEGORY_NAME, TEST_SEARCH_OPTIONS)
        }

        verify(exactly = 1) {
            coreEngine.search(
                eq(""),
                eq(listOf(TEST_CATEGORY_NAME)),
                eq(TEST_SEARCH_OPTIONS),
                any()
            )
        }
    }

    @Test
    fun `Check engine for successful response`() {
        val results = listOf(
            createTestCoreSearchResult(center = Point.fromLngLat(10.0, 20.0)),
            createTestCoreSearchResult(center = Point.fromLngLat(30.0, 50.0)),
        )

        mockEngineResponse(createTestCoreSearchResponseSuccess(
            results = results
        ))

        val result = runBlocking {
            engine.search(TEST_CATEGORY_NAME, TEST_SEARCH_OPTIONS)
        }

        assertTrue(result.isValue)
        assertEquals(results.map { it.mapToBase() }, result.value?.first?.map { it.rawSearchResult })
    }

    @Test
    fun `Check engine for error response`() {
        mockEngineResponse(
            createTestCoreSearchResponseHttpError(TEST_HTTP_ERROR_CODE, TEST_HTTP_ERROR_MESSAGE)
        )

        val result = runBlocking {
            engine.search(TEST_CATEGORY_NAME, TEST_SEARCH_OPTIONS)
        }

        assertTrue(result.isError)
        assertEquals(
            SearchRequestException(
                code = TEST_HTTP_ERROR_CODE,
                message = TEST_HTTP_ERROR_MESSAGE
            ), result.error
        )
    }

    private fun mockEngineResponse(response: SearchResponse) {
        val callbackSlot = slot<SearchCallback>()
        every { coreEngine.search(any(), any(), any(), capture(callbackSlot)) }.answers {
            callbackSlot.captured.run(response)
            TEST_REQUEST_ID
        }
    }

    private companion object {
        const val TEST_REQUEST_ID = 1L

        const val TEST_HTTP_ERROR_CODE = 500
        const val TEST_HTTP_ERROR_MESSAGE = "Test server error"
        const val TEST_CATEGORY_NAME = "test-category-name"
        val TEST_SEARCH_OPTIONS = createTestCoreSearchOptions()
    }
}
