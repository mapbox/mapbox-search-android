package com.mapbox.search

import android.util.Log
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.common.TileRegion
import com.mapbox.common.TileRegionError
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileRegionLoadProgress
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreObserver
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.common.tests.BuildConfig
import com.mapbox.search.result.BaseSearchResult
import com.mapbox.search.result.OriginalResultType
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.tests_support.BlockingEngineReadyCallback
import com.mapbox.search.tests_support.BlockingOnIndexChangeListener
import com.mapbox.search.tests_support.BlockingSearchCallback
import com.mapbox.search.tests_support.BlockingSearchCallback.SearchEngineResult
import com.mapbox.search.tests_support.EmptySearchCallback
import com.mapbox.search.tests_support.compareSearchResultWithServerSearchResult
import com.mapbox.search.tests_support.createTestOriginalSearchResult
import com.mapbox.search.tests_support.getAllTileRegionsBlocking
import com.mapbox.search.tests_support.loadTileRegionBlocking
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.tests_support.record.getAllBlocking
import com.mapbox.search.tests_support.removeTileRegionBlocking
import com.mapbox.search.tests_support.reverseGeocodingBlocking
import com.mapbox.search.tests_support.searchBlocking
import com.mapbox.search.utils.TimeProvider
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.Executor

@Suppress("LargeClass")
internal class OfflineSearchEngineIntegrationTest : BaseTest() {

    private val callbacksExecutor: Executor = SearchSdkMainThreadWorker.mainExecutor

    private val testTimeProvider = TimeProvider { CURRENT_TIME_MILLIS }
    private lateinit var searchEngine: OfflineSearchEngine

    private val debugTileStoreObserver = object : TileStoreObserver {
        override fun onRegionLoadProgress(id: String, progress: TileRegionLoadProgress) {
            Log.d(LOG_TAG, "onRegionLoadProgress($id, $progress)")
        }

        override fun onRegionLoadFinished(id: String, region: Expected<TileRegionError, TileRegion>) {
            Log.d(LOG_TAG, "onRegionLoadFinished($id, $region)")
        }

        override fun onRegionRemoved(id: String) {
            Log.d(LOG_TAG, "onRegionRemoved($id)")
        }

        override fun onRegionGeometryChanged(id: String, geometry: Geometry) {
            Log.d(LOG_TAG, "onRegionGeometryChanged($id, $geometry)")
        }

        override fun onRegionMetadataChanged(id: String, value: Value) {
            Log.d(LOG_TAG, "onRegionMetadataChanged($id, $value)")
        }
    }

    @Before
    override fun setUp() {
        super.setUp()

        MapboxSearchSdk.initializeInternal(
            application = targetApplication,
            accessToken = BuildConfig.MAPBOX_API_TOKEN,
            locationEngine = FixedPointLocationEngine(MAPBOX_DC_LOCATION),
            offlineSearchEngineSettings = OfflineSearchEngineSettings(tileStore = tileStore),
            timeProvider = testTimeProvider,
            allowReinitialization = true,
        )

        searchEngine = MapboxSearchSdk.getOfflineSearchEngine()

        tileStore.addObserver(debugTileStoreObserver)

        waitUntilEngineReady()
    }

    private fun waitUntilEngineReady() {
        val engineReadyCallback = BlockingEngineReadyCallback()
        searchEngine.addEngineReadyCallback(engineReadyCallback)
        engineReadyCallback.getResultBlocking()
    }

    @After
    override fun tearDown() {
        clearOfflineData()
        tileStore.removeObserver(debugTileStoreObserver)
    }

    @Test
    fun complexForwardReverseTest() {
        val listener = BlockingOnIndexChangeListener(1)
        searchEngine.addOnIndexChangeListener(listener)

        val descriptors = listOf(searchEngine.createTilesetDescriptor())

        val dcLoadOptions = TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(MAPBOX_DC_LOCATION)
            .acceptExpired(true)
            .build()

        val loadTilesResult = tileStore.loadTileRegionBlocking(TEST_GROUP_ID, dcLoadOptions)
        assertTrue(loadTilesResult.isValue)

        val tileRegion = requireNotNull(loadTilesResult.value)
        assertEquals(TEST_GROUP_ID, tileRegion.id)
        assertTrue(tileRegion.completedResourceCount > 0)
        assertEquals(tileRegion.requiredResourceCount, tileRegion.completedResourceCount)

        val events = listener.getResultsBlocking()
        events.forEach {
            val result = (it as? BlockingOnIndexChangeListener.OnIndexChangeResult.Result)
            assertNotNull(result)
            assertTrue(
                "Event type should be ADD, but was ${result!!.event.type}",
                result.event.type == OfflineIndexChangeEvent.EventType.ADD
            )
        }

        // Forward geocoding
        val searchResult = searchEngine.searchBlocking("1")
        assertTrue(searchResult is SearchEngineResult.Results)
        assertTrue(searchResult.requireResults().isNotEmpty())

        // Reverse geocoding
        val reverseResult = searchEngine.reverseGeocodingBlocking(searchResult.requireResults().first().coordinate!!)
        assertTrue(reverseResult is SearchEngineResult.Results)
        assertTrue(reverseResult.requireResults().isNotEmpty())
    }

    private fun loadOfflineData() {
        val listener = BlockingOnIndexChangeListener(1)
        searchEngine.addOnIndexChangeListener(listener)

        val descriptors = listOf(searchEngine.createTilesetDescriptor())

        val dcLoadOptions = TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(MAPBOX_DC_LOCATION)
            .acceptExpired(true)
            .build()

        val loadTilesResult = tileStore.loadTileRegionBlocking(TEST_GROUP_ID, dcLoadOptions)
        assertTrue(loadTilesResult.isValue)

        val tileRegion = requireNotNull(loadTilesResult.value)
        assertEquals(TEST_GROUP_ID, tileRegion.id)
        assertTrue(tileRegion.completedResourceCount > 0)
        assertEquals(tileRegion.requiredResourceCount, tileRegion.completedResourceCount)

        val events = listener.getResultsBlocking()
        events.forEach {
            val result = (it as? BlockingOnIndexChangeListener.OnIndexChangeResult.Result)
            assertNotNull(result)
            assertTrue(
                "Event type should be ADD, but was ${result!!.event.type}",
                result.event.type == OfflineIndexChangeEvent.EventType.ADD
            )
        }

        searchEngine.removeOnIndexChangeListener(listener)
    }

    private fun clearOfflineData() {
        val regions = tileStore.getAllTileRegionsBlocking().value
        if (regions == null || regions.isEmpty()) {
            return
        }

        val listener = BlockingOnIndexChangeListener(regions.size)
        searchEngine.addOnIndexChangeListener(listener)

        regions.forEach {
            tileStore.removeTileRegionBlocking(it.id)
        }

        val events = listener.getResultsBlocking()
        events.forEach {
            val result = (it as? BlockingOnIndexChangeListener.OnIndexChangeResult.Result)
            assertNotNull(result)
            assertTrue(
                "Event type should be REMOVE, but was ${result!!.event.type}",
                result.event.type == OfflineIndexChangeEvent.EventType.REMOVE
            )
        }

        searchEngine.removeOnIndexChangeListener(listener)
    }

    @Test
    fun testSearchRequestWithoutAddedRegions() {
        val result = searchEngine.searchBlocking("123")
        assertTrue(result is SearchEngineResult.Error)
    }

    @Test
    fun testReverseGeocodingWithoutAddedRegions() {
        val callback = BlockingSearchCallback()
        searchEngine.reverseGeocoding(
            OfflineReverseGeoOptions(center = MAPBOX_DC_LOCATION),
            callback
        )
        assertTrue(callback.getResultBlocking() is SearchEngineResult.Error)
    }

    @Test
    fun testDataLoading() {
        val descriptors = listOf(searchEngine.createTilesetDescriptor())

        val dcLoadOptions = TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(MAPBOX_DC_LOCATION)
            .acceptExpired(true)
            .build()

        val loadTilesResult = tileStore.loadTileRegionBlocking(TEST_GROUP_ID, dcLoadOptions)
        assertTrue(loadTilesResult.isValue)

        val tileRegion = requireNotNull(loadTilesResult.value)
        assertEquals(TEST_GROUP_ID, tileRegion.id)
        assertTrue(tileRegion.completedResourceCount > 0)
        assertEquals(tileRegion.requiredResourceCount, tileRegion.completedResourceCount)

        val allTileRegionsResult = tileStore.getAllTileRegionsBlocking()
        assertTrue(allTileRegionsResult.isValue)

        val allTileRegions = requireNotNull(allTileRegionsResult.value)
        assertEquals(1, allTileRegions.size)
        assertEquals(TEST_GROUP_ID, allTileRegions.first().id)
    }

    @Test
    fun testDataRemoval() {
        loadOfflineData()

        val listener = BlockingOnIndexChangeListener(1)
        searchEngine.addOnIndexChangeListener(listener)

        var allTileRegionsResult = tileStore.getAllTileRegionsBlocking()
        assertTrue(allTileRegionsResult.isValue)

        val removeResult = tileStore.removeTileRegionBlocking(TEST_GROUP_ID)
        assertTrue(removeResult.isValue)

        allTileRegionsResult = tileStore.getAllTileRegionsBlocking()
        assertTrue(allTileRegionsResult.isValue)

        val allTileRegions = requireNotNull(allTileRegionsResult.value)
        assertEquals(0, allTileRegions.size)

        val events = listener.getResultsBlocking()
        events.forEach {
            val result = (it as? BlockingOnIndexChangeListener.OnIndexChangeResult.Result)
            assertNotNull(result)
            assertTrue(
                "Event type should be REMOVE, but was ${result!!.event.type}",
                result.event.type == OfflineIndexChangeEvent.EventType.REMOVE
            )
        }

        searchEngine.removeOnIndexChangeListener(listener)

        val searchResult = searchEngine.searchBlocking(
            "2011 15th Street Northwest, Washington, District of Columbia",
            OfflineSearchOptions(origin = TEST_SEARCH_RESULT_MAPBOX.center),
        )

        assertTrue(searchResult is SearchEngineResult.Error)
        val error = searchResult as SearchEngineResult.Error
        assertTrue(error.e.message?.contains("Offline regions not added") == true)
    }

    @Test
    fun testSuccessfulSearchEmptyResponse() {
        loadOfflineData()

        val searchResult = searchEngine.searchBlocking(
            "Champ de Mars, 5 Avenue Anatole France, 75007 Paris, France",
            OfflineSearchOptions(),
        )

        assertTrue(searchResult is SearchEngineResult.Results)
        assertEquals(0, searchResult.requireResults().size)
    }

    @Test
    fun testSuccessfulSearch() {
        loadOfflineData()

        val historyDataProvider = MapboxSearchSdk.serviceProvider.historyDataProvider()
        historyDataProvider.clearBlocking(callbacksExecutor)

        val searchEngineResult = searchEngine.searchBlocking(
            "2011 15th Street Northwest, Washington, District of Columbia",
            OfflineSearchOptions(origin = TEST_SEARCH_RESULT_MAPBOX.center),
        )

        assertTrue(searchEngineResult is SearchEngineResult.Results)

        val results = searchEngineResult.requireResults()
        assertEquals(9, results.size)

        val result = results.first()
        val originalSearchResult = (result as BaseSearchResult).originalSearchResult
        val expectedSearchResult = TEST_SEARCH_RESULT_MAPBOX.copy(id = originalSearchResult.id)
        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchResult, originalSearchResult))

        val historyRecords = historyDataProvider.getAllBlocking(callbacksExecutor)

        assertEquals(
            "No item added to search history",
            0,
            historyRecords.size
        )
    }

    @Test
    fun testForwardGeocodingShortQuery() {
        loadOfflineData()

        val searchResult = searchEngine.searchBlocking("1")
        assertTrue(searchResult is SearchEngineResult.Results)
        assertEquals(10, searchResult.requireResults().size)
    }

    @Test
    fun testSuccessfulReverseGeocoding() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.reverseGeocoding(
            OfflineReverseGeoOptions(center = TEST_SEARCH_RESULT_MAPBOX.center!!),
            callback
        )

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertEquals(1, results.size)

        val originalSearchResult = (results.first() as BaseSearchResult).originalSearchResult
        val expectedSearchResult = TEST_SEARCH_RESULT_MAPBOX.copy(id = originalSearchResult.id)
        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchResult, originalSearchResult))
    }

    @Test
    fun testSuccessfulReverseGeocodingEmptyResponse() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.reverseGeocoding(
            OfflineReverseGeoOptions(center = Point.fromLngLat(2.2943982184367453, 48.85829192244227)),
            callback
        )

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertEquals(0, results.size)
    }

    @Test
    fun testSuccessfulAddressesSearch() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.searchAddressesNearby(
            street = TEST_SEARCH_RESULT_MAPBOX.addresses?.first()?.street!!,
            proximity = TEST_SEARCH_RESULT_MAPBOX.center!!,
            radiusMeters = 100.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertTrue(results.isNotEmpty())

        val originalSearchResult = (results.first() as BaseSearchResult).originalSearchResult
        val expectedResult = TEST_SEARCH_RESULT_MAPBOX.copy(id = originalSearchResult.id, distanceMeters = null)
        assertTrue(
            compareSearchResultWithServerSearchResult(
                expectedResult,
                (results.first() as BaseSearchResult).originalSearchResult
            )
        )
    }

    @Test
    fun testAddressSearchOutsideAddedOfflineRegion() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.searchAddressesNearby(
            street = "Champ de Mars, 5 Avenue Anatole France, 75007 Paris, France",
            proximity = Point.fromLngLat(2.294464, 48.858353),
            radiusMeters = 1000.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertTrue(results.isEmpty())
    }

    @Test
    fun testAddressSearchWithUnknownStreet() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.searchAddressesNearby(
            street = "Rue de Marseille, Paris, France",
            proximity = TEST_SEARCH_RESULT_MAPBOX.center!!,
            radiusMeters = 1000.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertTrue(results.isEmpty())
    }

    @Test
    fun testAddressSearchWithProximityOutsideOfAddedRegion() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.searchAddressesNearby(
            street = TEST_SEARCH_RESULT_MAPBOX.addresses?.first()?.street!!,
            proximity = Point.fromLngLat(2.294464, 48.858353),
            radiusMeters = 1000.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertTrue(results.isEmpty())
    }

    @Test
    fun testAddressesSearchNegativeRadius() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.searchAddressesNearby(
            street = TEST_SEARCH_RESULT_MAPBOX.addresses?.first()?.street!!,
            proximity = TEST_SEARCH_RESULT_MAPBOX.center!!,
            radiusMeters = -100.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Error)

        val exception = (callback.getResultBlocking() as SearchEngineResult.Error).e
        assertEquals("Negative radius", exception.message)
    }

    @Test
    fun testAddressesSearchZeroRadius() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.searchAddressesNearby(
            street = TEST_SEARCH_RESULT_MAPBOX.addresses?.first()?.street!!,
            proximity = TEST_SEARCH_RESULT_MAPBOX.center!!,
            radiusMeters = 0.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun testAddressesInvalidProximity() {
        loadOfflineData()

        val callback = BlockingSearchCallback()

        searchEngine.searchAddressesNearby(
            street = TEST_SEARCH_RESULT_MAPBOX.addresses?.first()?.street!!,
            proximity = Point.fromLngLat(300.0, 300.0),
            radiusMeters = 1000.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

        // Probably we should get error here (related to an issue in search-sdk/#578)
        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertTrue(results.isEmpty())
    }

    @Test
    fun testConsecutiveRequests() {
        val task1 = searchEngine.search("Baker street", OfflineSearchOptions(), EmptySearchCallback)

        val callback = BlockingSearchCallback()
        val task2 = searchEngine.search("Baker street", OfflineSearchOptions(), callback)
        callback.getResultBlocking()

        assertTrue(task1.isCancelled)
        assertTrue(task2.isDone)
    }

    companion object {

        private val tileStore: TileStore = TileStore.create()

        private const val LOG_TAG = "OfflineSearchEngineIntegrationTest"

        private const val TEST_GROUP_ID = "usa-dc"
        private val MAPBOX_DC_LOCATION: Point = Point.fromLngLat(-77.03399849939174, 38.89992081005698)

        private const val CURRENT_TIME_MILLIS = 123L

        private val TEST_SEARCH_RESULT_MAPBOX = createTestOriginalSearchResult(
            id = "<will be dynamically changed>",
            types = listOf(OriginalResultType.ADDRESS),
            names = listOf("2011 15th Street Northwest"),
            languages = listOf("def"),
            addresses = listOf(
                SearchAddress(
                    houseNumber = "2011",
                    street = "15th Street Northwest",
                    place = "Washington",
                    region = "District of Columbia",
                )
            ),
            descriptionAddress = "2011 15th Street Northwest, Washington, District of Columbia",
            distanceMeters = 0.0,
            center = Point.fromLngLat(-77.03401323039313, 38.91793457393481),
            serverIndex = null,
        )

        @BeforeClass
        @JvmStatic
        fun cleanUpBeforeTests() {
            tileStore.getAllTileRegionsBlocking().value!!.forEach {
                tileStore.removeTileRegionBlocking(it.id)
            }
            Thread.sleep(1000)
        }
    }
}
