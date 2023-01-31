package com.mapbox.search.offline

import android.app.Application
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
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
import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.result.mapToCore
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.common.compareSearchResultWithServerSearchResult
import com.mapbox.search.common.createCoreSearchAddress
import com.mapbox.search.common.createTestCoreSearchResult
import com.mapbox.search.offline.test.R
import com.mapbox.search.offline.tests_support.BlockingEngineReadyCallback
import com.mapbox.search.offline.tests_support.BlockingOfflineSearchCallback
import com.mapbox.search.offline.tests_support.BlockingOfflineSearchCallback.SearchEngineResult
import com.mapbox.search.offline.tests_support.BlockingOnIndexChangeListener
import com.mapbox.search.offline.tests_support.getAllTileRegionsBlocking
import com.mapbox.search.offline.tests_support.loadTileRegionBlocking
import com.mapbox.search.offline.tests_support.removeTileRegionBlocking
import com.mapbox.search.offline.tests_support.reverseGeocodingBlocking
import com.mapbox.search.offline.tests_support.searchBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

@Suppress("LargeClass")
internal class OfflineSearchEngineIntegrationTest {

    private val targetApplication: Application
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

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
    fun setUp() {
        searchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(
                accessToken = targetApplication.getString(R.string.mapbox_access_token),
                tileStore = tileStore,
                locationEngine = FixedPointLocationEngine(MAPBOX_DC_LOCATION)
            )
        )

        tileStore.addObserver(debugTileStoreObserver)

        waitUntilEngineReady()
    }

    private fun waitUntilEngineReady() {
        val engineReadyCallback = BlockingEngineReadyCallback()
        searchEngine.addEngineReadyCallback(engineReadyCallback)
        engineReadyCallback.getResultBlocking()
    }

    @After
    fun tearDown() {
        clearOfflineData()
        tileStore.removeObserver(debugTileStoreObserver)
    }

    @Test
    fun complexForwardReverseTest() {
        val listener = BlockingOnIndexChangeListener(1)
        searchEngine.addOnIndexChangeListener(listener)

        val descriptors = listOf(OfflineSearchEngine.createTilesetDescriptor())

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
        val reverseResult = searchEngine.reverseGeocodingBlocking(searchResult.requireResults().first().coordinate)
        assertTrue(reverseResult is SearchEngineResult.Results)
        assertTrue(reverseResult.requireResults().isNotEmpty())
    }

    private fun loadOfflineData() {
        val listener = BlockingOnIndexChangeListener(1)
        searchEngine.addOnIndexChangeListener(listener)

        val descriptors = listOf(OfflineSearchEngine.createTilesetDescriptor())

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
        val callback = BlockingOfflineSearchCallback()
        searchEngine.reverseGeocoding(
            OfflineReverseGeoOptions(center = MAPBOX_DC_LOCATION),
            callback
        )
        assertTrue(callback.getResultBlocking() is SearchEngineResult.Error)
    }

    @Test
    fun testDataLoading() {
        val descriptors = listOf(OfflineSearchEngine.createTilesetDescriptor())

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

        val searchEngineResult = searchEngine.searchBlocking(
            "2011 15th Street Northwest, Washington, District of Columbia",
            OfflineSearchOptions(origin = TEST_SEARCH_RESULT_MAPBOX.center),
        )

        assertTrue(searchEngineResult is SearchEngineResult.Results)

        val results = searchEngineResult.requireResults()
        assertEquals(10, results.size)

        val result = results.first()
        val rawSearchResult = result.rawSearchResult
        val expectedSearchResult = TEST_SEARCH_RESULT_MAPBOX.copy(id = rawSearchResult.id)
        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchResult.mapToCore(), rawSearchResult.mapToCore()))
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

        val callback = BlockingOfflineSearchCallback()

        searchEngine.reverseGeocoding(
            OfflineReverseGeoOptions(center = TEST_SEARCH_RESULT_MAPBOX.center!!),
            callback
        )

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertEquals(1, results.size)

        val rawSearchResult = results.first().rawSearchResult
        val expectedSearchResult = TEST_SEARCH_RESULT_MAPBOX.copy(id = rawSearchResult.id)
        assertTrue(compareSearchResultWithServerSearchResult(expectedSearchResult.mapToCore(), rawSearchResult.mapToCore()))
    }

    @Test
    fun testSuccessfulReverseGeocodingEmptyResponse() {
        loadOfflineData()

        val callback = BlockingOfflineSearchCallback()

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

        val callback = BlockingOfflineSearchCallback()

        searchEngine.searchAddressesNearby(
            street = TEST_SEARCH_RESULT_MAPBOX.addresses?.first()?.street!!,
            proximity = TEST_SEARCH_RESULT_MAPBOX.center!!,
            radiusMeters = 100.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

        val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
        assertTrue(results.isNotEmpty())

        val rawSearchResult = results.first().rawSearchResult
        val expectedResult = TEST_SEARCH_RESULT_MAPBOX.copy(id = rawSearchResult.id, distanceMeters = null)
        assertTrue(
            compareSearchResultWithServerSearchResult(
                expectedResult.mapToCore(),
                results.first().rawSearchResult.mapToCore()
            )
        )
    }

    @Test
    fun testAddressSearchOutsideAddedOfflineRegion() {
        loadOfflineData()

        val callback = BlockingOfflineSearchCallback()

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

        val callback = BlockingOfflineSearchCallback()

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

        val callback = BlockingOfflineSearchCallback()

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

        val callback = BlockingOfflineSearchCallback()

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

        val callback = BlockingOfflineSearchCallback()

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

        val callback = BlockingOfflineSearchCallback()

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

    companion object {

        private val tileStore: TileStore = TileStore.create()

        private const val LOG_TAG = "OfflineSearchEngineIntegrationTest"

        private const val TEST_GROUP_ID = "usa-dc"
        private val MAPBOX_DC_LOCATION: Point = Point.fromLngLat(-77.03399849939174, 38.89992081005698)

        private val TEST_CORE_SEARCH_RESULT = createTestCoreSearchResult(
            id = "<will be dynamically changed>",
            types = listOf(CoreResultType.ADDRESS),
            names = listOf("2011 15th Street Northwest"),
            languages = listOf("def"),
            addresses = listOf(
                createCoreSearchAddress(
                    houseNumber = "2011",
                    street = "15th Street Northwest",
                    place = "Washington",
                    region = "District of Columbia",
                )
            ),
            descriptionAddress = "2011 15th Street Northwest, Washington, District of Columbia",
            distanceMeters = 0.0,
            center = Point.fromLngLat(-77.03402549505554, 38.91792475903431),
            serverIndex = null,
        )

        private val TEST_SEARCH_RESULT_MAPBOX = TEST_CORE_SEARCH_RESULT.mapToBase()

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
