package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.SearchResultSuggestAction
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.createTestHistoryRecord
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.equalsTo
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.upsertAllBlocking
import com.mapbox.search.utils.TestAnalyticsService
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor

internal class SearchEngineBatchRetrieveTest : BaseTest() {

    private lateinit var mockServer: MockWebServer
    private lateinit var searchEngine: SearchEngine
    private lateinit var historyDataProvider: HistoryDataProvider
    private lateinit var favoritesDataProvider: FavoritesDataProvider
    private val analyticsService: TestAnalyticsService = TestAnalyticsService()
    private val callbacksExecutor: Executor = SearchSdkMainThreadWorker.mainExecutor

    @Before
    override fun setUp() {
        super.setUp()

        mockServer = MockWebServer()

        MapboxSearchSdk.initializeInternal(targetApplication)

        val searchEngineSettings = SearchEngineSettings(
            applicationContext = targetApplication,
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            singleBoxSearchBaseUrl = mockServer.url("").toString()
        )

        searchEngine = MapboxSearchSdk.createSearchEngineWithBuiltInDataProvidersBlocking(
            apiType = ApiType.SBS,
            settings = searchEngineSettings,
            analyticsService = analyticsService
        )

        historyDataProvider = MapboxSearchSdk.serviceProvider.historyDataProvider()
        historyDataProvider.clearBlocking(callbacksExecutor)

        favoritesDataProvider = MapboxSearchSdk.serviceProvider.favoritesDataProvider()
        favoritesDataProvider.clearBlocking(callbacksExecutor)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptySuggestionsRetrieval() {
        val callback = BlockingSearchSelectionCallback()
        searchEngine.select(emptyList(), callback)
    }

    @Test
    fun testSuggestionsFromDifferentSearchResultsRetrieval() {
        val callback = BlockingSearchSelectionCallback()
        val allSuggestions = mutableListOf<SearchSuggestion>()
        val responseUuidList = mutableListOf<String?>()

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/suggestions-single-poi.json"))
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/suggestions-mixed-types.json"))

        repeat(2) {
            searchEngine.search(TEST_QUERY, SearchOptions(), callback)
            val suggestionsResult = callback.getResultBlocking()
            assertTrue(suggestionsResult is BlockingSearchSelectionCallback.SearchEngineResult.Suggestions)
            val suggestions = (suggestionsResult as BlockingSearchSelectionCallback.SearchEngineResult.Suggestions).suggestions
            allSuggestions += suggestions.filter { it.isBatchResolveSupported }
            responseUuidList += suggestions.first().requestOptions.requestContext.responseUuid
            callback.reset()
        }

        assertEquals(4, allSuggestions.size)
        assertEquals(true, allSuggestions.all { it.isBatchResolveSupported })
        assertEquals(2, responseUuidList.distinct().size)

        searchEngine.select(allSuggestions, callback)

        val batchResult = callback.getResultBlocking()
        val (error) = batchResult as BlockingSearchSelectionCallback.SearchEngineResult.Error

        assertTrue(
            "Error should be IllegalArgumentException",
            error.equalsTo(
                IllegalArgumentException("All provided suggestions must originate from the same search result!")
            )
        )
    }

    @Test
    fun testSinglePoiRetrieve() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/suggestions-single-poi.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestionsResult = callback.getResultBlocking()
        assertTrue(suggestionsResult is BlockingSearchSelectionCallback.SearchEngineResult.Suggestions)
        val suggestions = (suggestionsResult as BlockingSearchSelectionCallback.SearchEngineResult.Suggestions).suggestions

        assertEquals(1, suggestions.size)
        assertEquals(true, suggestions[0].isBatchResolveSupported)

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/retrieved-single-poi.json"))

        callback.reset()
        searchEngine.select(suggestions, callback)

        val batchResult = callback.getResultBlocking()
        val (resolved, responseInfo) = batchResult as BlockingSearchSelectionCallback.SearchEngineResult.BatchResult

        assertEquals(1, resolved.size)
        assertNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testMixedTypesSuggestionsRetrieval() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/suggestions-mixed-types.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestionsResult = callback.getResultBlocking()
        assertTrue(suggestionsResult is BlockingSearchSelectionCallback.SearchEngineResult.Suggestions)
        val suggestions = (suggestionsResult as BlockingSearchSelectionCallback.SearchEngineResult.Suggestions).suggestions

        assertEquals(4, suggestions.size)
        assertEquals(true, suggestions[0].isBatchResolveSupported)
        assertEquals(true, suggestions[1].isBatchResolveSupported)
        assertEquals(true, suggestions[2].isBatchResolveSupported)
        assertEquals(false, suggestions[3].isBatchResolveSupported)

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/retrieved-for-mixed-types.json"))

        callback.reset()
        searchEngine.select(suggestions, callback)

        val batchResult = callback.getResultBlocking()
        val (resolved, responseInfo) = batchResult as BlockingSearchSelectionCallback.SearchEngineResult.BatchResult

        assertEquals(3, resolved.size)
        assertNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testIndexableRecordsOnlyRetrieval() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-empty.json"))

        val records = (1..3).map {
            createTestHistoryRecord(id = "id$it", name = "$TEST_QUERY $it")
        }

        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(limit = records.size), callback)

        val suggestionsResult = callback.getResultBlocking()
        assertTrue(suggestionsResult is BlockingSearchSelectionCallback.SearchEngineResult.Suggestions)
        val suggestions = (suggestionsResult as BlockingSearchSelectionCallback.SearchEngineResult.Suggestions).suggestions

        assertEquals(records.size, suggestions.size)
        suggestions.forEach {
            assertTrue((it.type as? SearchSuggestionType.IndexableRecordItem)?.isHistoryRecord == true)
        }

        callback.reset()
        searchEngine.select(suggestions, callback)

        val batchResult = callback.getResultBlocking()
        val (resolved, responseInfo) = batchResult as BlockingSearchSelectionCallback.SearchEngineResult.BatchResult

        assertEquals(records.size, resolved.size)
        resolved.forEachIndexed { index, resolvedResult ->
            assertEquals(records[index], (resolvedResult as IndexableRecordSearchResult).record)
        }
        assertNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testAllTypesShuffledRetrieval() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/suggestions-mixed-types.json"))

        val records = (1..3).map {
            createTestHistoryRecord(id = "history_item_$it", name = "$TEST_QUERY $it")
        }

        historyDataProvider.upsertAllBlocking(records, callbacksExecutor)

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestionsResult = callback.getResultBlocking()
        assertTrue(suggestionsResult is BlockingSearchSelectionCallback.SearchEngineResult.Suggestions)
        val suggestions = (suggestionsResult as BlockingSearchSelectionCallback.SearchEngineResult.Suggestions).suggestions

        assertEquals(7, suggestions.size)
        assertEquals("history_item_1", suggestions[0].id)
        assertEquals("history_item_2", suggestions[1].id)
        assertEquals("history_item_3", suggestions[2].id)
        assertEquals("id_test_poi_1", suggestions[3].id)
        assertEquals("id_test_poi_2", suggestions[4].id)
        assertEquals("id_test_poi_3", suggestions[5].id)
        assertEquals("id_test_street_1", suggestions[6].id)

        val shuffledSuggestions = listOf(
            suggestions[3], // id_test_poi_1
            suggestions[6], // id_test_street_1
            suggestions[2], // history_item_3
            suggestions[4], // id_test_poi_2
            suggestions[1], // history_item_2
            suggestions[5], // id_test_poi_3
            suggestions[0], // history_item_1
        )

        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/retrieved-for-mixed-types.json"))

        callback.reset()
        searchEngine.select(shuffledSuggestions, callback)

        val batchResult = callback.getResultBlocking()
        val (resolved, responseInfo) = batchResult as BlockingSearchSelectionCallback.SearchEngineResult.BatchResult

        assertEquals(6, resolved.size)
        shuffledSuggestions
            .filter { it.id != "id_test_street_1" }
            .forEachIndexed { index, suggestion ->
                assertEquals(suggestion.id, resolved[index].id)
            }
        assertNull(responseInfo.coreSearchResponse)
    }

    @Test
    fun testErrorResponse() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/batch/suggestions-mixed-types.json"))

        val callback = BlockingSearchSelectionCallback()
        searchEngine.search(TEST_QUERY, SearchOptions(), callback)

        val suggestionsResult = callback.getResultBlocking()
        assertTrue(suggestionsResult is BlockingSearchSelectionCallback.SearchEngineResult.Suggestions)
        val suggestions = (suggestionsResult as BlockingSearchSelectionCallback.SearchEngineResult.Suggestions).suggestions

        assertEquals(4, suggestions.size)

        mockServer.enqueue(MockResponse().setResponseCode(404))

        callback.reset()
        searchEngine.select(suggestions, callback)

        val res = callback.getResultBlocking()
        assertTrue(res is BlockingSearchSelectionCallback.SearchEngineResult.Error && res.e is SearchRequestException && res.e.code == 404)
        res as BlockingSearchSelectionCallback.SearchEngineResult.Error

        if (!BuildConfig.DEBUG) {
            assertEquals(analyticsService.capturedErrors, listOf(res.e))
        }
    }

    @After
    override fun tearDown() {
        analyticsService.reset()
        mockServer.shutdown()
        super.tearDown()
    }

    private companion object {
        const val TEST_QUERY = "Starbucks"
        const val TEST_ACCESS_TOKEN = "pk.test"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)
        val TEST_ORIGINAL_SEARCH_RESULT = createTestOriginalSearchResult(
            action = SearchResultSuggestAction(
                endpoint = "test-endpoint-1",
                path = "test-path-1",
                query = "test-query-1",
                body = null,
                multiRetrievable = true
            )
        )
    }
}
