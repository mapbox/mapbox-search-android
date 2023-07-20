package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreApiType
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
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.result.isIndexableRecordSuggestion
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.record.CustomRecord
import com.mapbox.search.tests_support.record.StubRecordsStorage
import com.mapbox.search.tests_support.record.TestDataProvider
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getBlocking
import com.mapbox.search.tests_support.registerDataProviderBlocking
import com.mapbox.search.tests_support.searchBlocking
import com.mapbox.search.tests_support.selectBlocking
import com.mapbox.search.tests_support.unregisterDataProviderBlocking
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
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val customDataProvider = TestDataProvider(
            stubStorage = StubRecordsStorage(TEST_CUSTOM_USER_RECORDS.toMutableList())
        )
        val secondCustomRecord = TEST_CUSTOM_USER_RECORDS[1]

        val registrationResult = searchEngine.registerDataProviderBlocking(
            dataProvider = customDataProvider,
            executor = callbacksExecutor,
        )

        assertTrue(registrationResult.isResult)

        val options = SearchOptions(origin = secondCustomRecord.coordinate)
        val response = searchEngine.searchBlocking(secondCustomRecord.name, options)
        val suggestions = response.requireSuggestions()

        val first = suggestions.first()
        assertEquals(secondCustomRecord.id, first.id)
        assertEquals(secondCustomRecord.name, first.name)
        assertEquals(secondCustomRecord.descriptionText, first.descriptionText)
        assertEquals(SearchAddress(), first.address)
        assertEquals(
            SearchSuggestionType.IndexableRecordItem(
                secondCustomRecord, customDataProvider.dataProviderName
            ),
            first.type
        )

        val selectionResponse = searchEngine.selectBlocking(suggestions[0])
        val result = selectionResponse.requireResult().result

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
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val testException = RuntimeException("Test exception")
        val customDataProvider = TestDataProvider(
            stubStorage = StubRecordsStorage(TEST_CUSTOM_USER_RECORDS.toMutableList())
        ).apply {
            mode = TestDataProvider.Mode.Fail(testException)
        }
        val secondCustomRecord = TEST_CUSTOM_USER_RECORDS[1]

        val registrationResult = searchEngine.registerDataProviderBlocking(
            dataProvider = customDataProvider,
            executor = callbacksExecutor
        )

        assertTrue(registrationResult.isError)
        assertTrue(testException.equalsTo(registrationResult.requireError()))

        val options = SearchOptions(origin = secondCustomRecord.coordinate)
        val response = searchEngine.searchBlocking(secondCustomRecord.name, options)

        val suggestions = response.requireSuggestions()

        assertTrue(
            suggestions.none {
                it.isIndexableRecordSuggestion && it.id == secondCustomRecord.id
            }
        )
    }

    @Test
    fun testSuccessfulCustomProviderUnregistration() {
        mockServer.enqueue(createSuccessfulResponse("sbs_responses/forward/suggestions-successful.json"))

        val customDataProvider = TestDataProvider(
            stubStorage = StubRecordsStorage(TEST_CUSTOM_USER_RECORDS.toMutableList())
        )
        val secondCustomRecord = TEST_CUSTOM_USER_RECORDS[1]

        val registerResult = searchEngine.registerDataProviderBlocking(
            dataProvider = customDataProvider,
            executor = callbacksExecutor,
        )

        assertTrue(registerResult.isResult)

        val unregisterResult = searchEngine.unregisterDataProviderBlocking(
            dataProvider = customDataProvider,
            executor = callbacksExecutor,
        )
        assertTrue(unregisterResult.isResult)

        val options = SearchOptions(origin = secondCustomRecord.coordinate)
        val response = searchEngine.searchBlocking(secondCustomRecord.name, options)
        val suggestions = response.requireSuggestions()

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
                apiType = CoreApiType.SEARCH_BOX,
                keyboardLocale = TEST_KEYBOARD_LOCALE,
                screenOrientation = TEST_ORIENTATION,
                responseUuid = ""
            )
        )
    }
}
