package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.result.BaseIndexableRecordSearchSuggestion
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.utils.KeyboardLocaleProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.orientation.ScreenOrientation
import com.mapbox.search.base.utils.orientation.ScreenOrientationProvider
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.common.tests.equalsTo
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.mapToBase
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.isIndexableRecordSuggestion
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback
import com.mapbox.search.tests_support.BlockingSearchSelectionCallback.SearchEngineResult
import com.mapbox.search.tests_support.compareSearchResultWithServerSearchResult
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.tests_support.record.CustomRecord
import com.mapbox.search.tests_support.record.StubRecordsStorage
import com.mapbox.search.tests_support.record.TestDataProvider
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.concurrent.Executor

internal class CustomDataProviderTest : BaseTest() {

    private lateinit var mockServer: MockWebServer
    private lateinit var searchEngine: SearchEngine
    private lateinit var historyDataProvider: HistoryDataProvider
    private lateinit var favoritesDataProvider: FavoritesDataProvider
    private val timeProvider: TimeProvider = TimeProvider { TEST_LOCAL_TIME_MILLIS }
    private val keyboardLocaleProvider: KeyboardLocaleProvider = KeyboardLocaleProvider { TEST_KEYBOARD_LOCALE }
    private val orientationProvider: ScreenOrientationProvider = ScreenOrientationProvider { TEST_ORIENTATION }
    private val callbacksExecutor: Executor = SearchSdkMainThreadWorker.mainExecutor

    @Before
    override fun setUp() {
        super.setUp()

        mockServer = MockWebServer()

        MapboxSearchSdk.initialize(
            application = targetApplication,
            timeProvider = timeProvider,
            keyboardLocaleProvider = keyboardLocaleProvider,
            orientationProvider = orientationProvider,
        )

        val searchEngineSettings = SearchEngineSettings(
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            singleBoxSearchBaseUrl = mockServer.url("").toString()
        )

        searchEngine = createSearchEngineWithBuiltInDataProvidersBlocking(
            apiType = ApiType.SBS,
            settings = searchEngineSettings,
        )

        historyDataProvider = ServiceProvider.INSTANCE.historyDataProvider()
        historyDataProvider.clearBlocking(callbacksExecutor)

        favoritesDataProvider = ServiceProvider.INSTANCE.favoritesDataProvider()
        favoritesDataProvider.clearBlocking(callbacksExecutor)
    }

    @Test
    fun testSuccessfulCustomProviderRegistration() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val customDataProvider = TestDataProvider(
            stubStorage = StubRecordsStorage(TEST_CUSTOM_USER_RECORDS.toMutableList())
        )
        val secondCustomRecord = TEST_CUSTOM_USER_RECORDS[1]

        val blockingCallback = BlockingCompletionCallback<Unit>()

        searchEngine.registerDataProvider(
            dataProvider = customDataProvider,
            executor = callbacksExecutor,
            callback = blockingCallback,
        )

        assertTrue(blockingCallback.getResultBlocking().isResult)

        val callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = secondCustomRecord.coordinate)
        searchEngine.search(secondCustomRecord.name, options, callback)

        var searchResult = callback.getResultBlocking()
        assertTrue(searchResult is SearchEngineResult.Suggestions)
        val suggestions = (searchResult as SearchEngineResult.Suggestions).suggestions

        val expectedSuggestion = BaseIndexableRecordSearchSuggestion(
            record = secondCustomRecord.mapToBase(),
            rawSearchResult = createTestBaseRawSearchResult(
                id = secondCustomRecord.id,
                layerId = customDataProvider.dataProviderName,
                types = listOf(BaseRawResultType.USER_RECORD),
                names = listOf(secondCustomRecord.name),
                center = secondCustomRecord.coordinate,
                addresses = listOf(SearchAddress()),
                distanceMeters = 0.0,
                serverIndex = null,
            ),
            requestOptions = TEST_REQUEST_OPTIONS.copy(
                query = secondCustomRecord.name,
                options = options.copy(
                    origin = secondCustomRecord.coordinate
                ),
                requestContext = TEST_REQUEST_OPTIONS.requestContext.copy(
                    responseUuid = "bf62f6f4-92db-11eb-a8b3-0242ac130003"
                )
            ).mapToBase()
        ).mapToPlatform()
        assertTrue(
            compareSearchResultWithServerSearchResult(expectedSuggestion, suggestions[0])
        )

        callback.reset()
        searchEngine.select(suggestions[0], callback)

        searchResult = callback.getResultBlocking()
        assertTrue(searchResult is SearchEngineResult.Result)
        val result = (searchResult as SearchEngineResult.Result).result

        assertEquals(secondCustomRecord, result.indexableRecord)

        val historyFromCustomRecord = historyDataProvider.getBlocking(secondCustomRecord.id, callbacksExecutor)

        assertEquals(
            HistoryRecord(
                id = secondCustomRecord.id,
                name = secondCustomRecord.name,
                descriptionText = secondCustomRecord.descriptionText,
                address = secondCustomRecord.address ?: SearchAddress(),
                routablePoints = secondCustomRecord.routablePoints,
                categories = secondCustomRecord.categories,
                makiIcon = secondCustomRecord.makiIcon,
                coordinate = secondCustomRecord.coordinate,
                type = secondCustomRecord.type,
                metadata = secondCustomRecord.metadata,
                timestamp = TEST_LOCAL_TIME_MILLIS,
            ),
            historyFromCustomRecord
        )
    }

    @Test
    fun testFailCustomProviderRegistration() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val testException = RuntimeException("Test exception")
        val customDataProvider = TestDataProvider(
            stubStorage = StubRecordsStorage(TEST_CUSTOM_USER_RECORDS.toMutableList())
        ).apply {
            mode = TestDataProvider.Mode.Fail(testException)
        }
        val secondCustomRecord = TEST_CUSTOM_USER_RECORDS[1]

        val blockingCallback = BlockingCompletionCallback<Unit>()

        searchEngine.registerDataProvider(
            dataProvider = customDataProvider,
            executor = callbacksExecutor,
            callback = blockingCallback,
        )

        val res = blockingCallback.getResultBlocking()
        assertTrue(blockingCallback.getResultBlocking().isError)
        assertTrue(testException.equalsTo(res.requireError()))

        val callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = secondCustomRecord.coordinate)
        searchEngine.search(secondCustomRecord.name, options, callback)

        val searchResult = callback.getResultBlocking()
        assertTrue(searchResult is SearchEngineResult.Suggestions)
        val suggestions = (searchResult as SearchEngineResult.Suggestions).suggestions

        assertTrue(
            suggestions.none {
                it.isIndexableRecordSuggestion && it.id == secondCustomRecord.id
            }
        )
    }

    @Test
    fun testSuccessfulCustomProviderUnregistration() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/suggestions-successful-for-minsk.json"))

        val customDataProvider = TestDataProvider(
            stubStorage = StubRecordsStorage(TEST_CUSTOM_USER_RECORDS.toMutableList())
        )
        val secondCustomRecord = TEST_CUSTOM_USER_RECORDS[1]

        val blockingCallback = BlockingCompletionCallback<Unit>()

        searchEngine.registerDataProvider(
            dataProvider = customDataProvider,
            executor = callbacksExecutor,
            callback = blockingCallback,
        )

        assertTrue(blockingCallback.getResultBlocking().isResult)

        blockingCallback.reset()

        searchEngine.unregisterDataProvider(
            dataProvider = customDataProvider,
            executor = callbacksExecutor,
            callback = blockingCallback,
        )
        assertTrue(blockingCallback.getResultBlocking().isResult)

        val callback = BlockingSearchSelectionCallback()
        val options = SearchOptions(origin = secondCustomRecord.coordinate)
        searchEngine.search(secondCustomRecord.name, options, callback)

        val searchResult = callback.getResultBlocking()
        assertTrue(searchResult is SearchEngineResult.Suggestions)
        val suggestions = (searchResult as SearchEngineResult.Suggestions).suggestions

        assertTrue(
            suggestions.none {
                it.isIndexableRecordSuggestion && it.id == secondCustomRecord.id
            }
        )
    }

    @After
    override fun tearDown() {
        mockServer.shutdown()
        super.tearDown()
    }

    private companion object {

        val TEST_CUSTOM_USER_RECORDS = listOf(
            CustomRecord.create(
                name = "Let it be",
                coordinate = Point.fromLngLat(27.575321258282806, 53.89025545661358),
                provider = CustomRecord.Provider.CLOUD,
            ),
            CustomRecord.create(
                name = "Laŭka",
                coordinate = Point.fromLngLat(27.574862357961212, 53.88998973246244),
                provider = CustomRecord.Provider.CLOUD,
            ),
            CustomRecord.create(
                name = "Underdog",
                coordinate = Point.fromLngLat(27.57573285942709, 53.89020312748444),
                provider = CustomRecord.Provider.LOCAL,
            ),
        )

        const val TEST_ACCESS_TOKEN = "pk.test"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)

        const val TEST_LOCAL_TIME_MILLIS = 12345L
        const val TEST_UUID = "test-generated-uuid"
        val TEST_KEYBOARD_LOCALE: Locale = Locale.ENGLISH
        val TEST_ORIENTATION = ScreenOrientation.PORTRAIT

        val TEST_REQUEST_OPTIONS = RequestOptions(
            query = "",
            endpoint = "suggest",
            options = SearchOptions.Builder().build(),
            proximityRewritten = false,
            originRewritten = false,
            sessionID = TEST_UUID,
            requestContext = SearchRequestContext(
                apiType = CoreApiType.SBS,
                keyboardLocale = TEST_KEYBOARD_LOCALE,
                screenOrientation = TEST_ORIENTATION,
                responseUuid = ""
            )
        )
    }
}
