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
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.common.tests.createCoreSearchAddress
import com.mapbox.search.common.tests.createCoreSearchAddressCountry
import com.mapbox.search.common.tests.createCoreSearchAddressRegion
import com.mapbox.search.common.tests.createTestCoreSearchResult
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
import kotlin.math.abs

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
                tileStore = tileStore,
                locationProvider = FixedPointLocationEngine(MAPBOX_DC_LOCATION)
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
        assertTrue(
            "loadTilesResult should be valid",
            loadTilesResult.isValue
        )

        val tileRegion = requireNotNull(loadTilesResult.value)
        assertEquals(
            "tileRegionId should be $TEST_GROUP_ID, but was ${tileRegion.id}",
            TEST_GROUP_ID,
            tileRegion.id
        )
        assertTrue(
            "completedResourceCount should be greater than 0",
            tileRegion.completedResourceCount > 0
        )
        assertEquals(
            "requiredResourceCount (${tileRegion.requiredResourceCount}) != completedResourceCount (tileRegion.completedResourceCount)",
            tileRegion.requiredResourceCount,
            tileRegion.completedResourceCount
        )

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
            TEST_QUERY,
            OfflineSearchOptions(origin = TEST_SEARCH_RESULT_MAPBOX.coordinate),
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

    // See SSDK-501 for details
     @Test
     fun testSuccessfulSearch() {
         loadOfflineData()

         val searchEngineResult = searchEngine.searchBlocking(
             TEST_QUERY,
             OfflineSearchOptions(origin = TEST_SEARCH_RESULT_MAPBOX.coordinate),
         )

         assertTrue(searchEngineResult is SearchEngineResult.Results)

         val results = searchEngineResult.requireResults()
         assertTrue(results.size > 5)

         val result = results.first()
         assertTrue(
             assertSearchResultEquals(
                 TEST_SEARCH_RESULT_MAPBOX,
                 result
             )
         )
     }

    @Test
    fun testForwardGeocodingShortQuery() {
        loadOfflineData()

        val searchResult = searchEngine.searchBlocking("1")
        assertTrue(searchResult is SearchEngineResult.Results)
        assertEquals(10, searchResult.requireResults().size)
    }

    // See SSDK-501 for details
     @Test
     fun testSuccessfulReverseGeocoding() {
         loadOfflineData()

         val callback = BlockingOfflineSearchCallback()

         searchEngine.reverseGeocoding(
             OfflineReverseGeoOptions(center = TEST_SEARCH_RESULT_MAPBOX.coordinate),
             callback
         )

         val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
         assertEquals(results.size, 2)

         assertTrue(
             results.any {
                 try {
                     assertSearchResultEquals(
                         TEST_SEARCH_RESULT_MAPBOX,
                         it
                     )
                 } catch (e: AssertionError) {
                     false
                 }
             }
         )
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

    // See SSDK-501 for details
     @Test
     fun testSuccessfulAddressesSearch() {
         loadOfflineData()

         val callback = BlockingOfflineSearchCallback()

         searchEngine.searchAddressesNearby(
             street = TEST_SEARCH_RESULT_MAPBOX.address?.street!!,
             proximity = TEST_SEARCH_RESULT_MAPBOX.coordinate,
             radiusMeters = 100.0,
             callback
         )

         assertTrue(callback.getResultBlocking() is SearchEngineResult.Results)

         val results = (callback.getResultBlocking() as SearchEngineResult.Results).results
         assertTrue(results.isNotEmpty())

         val expectedResult = OfflineSearchResult(TEST_SEARCH_RESULT_MAPBOX.rawSearchResult.copy(distanceMeters = null))
         assertTrue(
             assertSearchResultEquals(
                 expectedResult,
                 results.first()
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
            proximity = TEST_SEARCH_RESULT_MAPBOX.coordinate,
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
            street = TEST_SEARCH_RESULT_MAPBOX.address?.street!!,
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
            street = TEST_SEARCH_RESULT_MAPBOX.address?.street!!,
            proximity = TEST_SEARCH_RESULT_MAPBOX.coordinate,
            radiusMeters = -100.0,
            callback
        )

        assertTrue(callback.getResultBlocking() is SearchEngineResult.Error)

        val exception = (callback.getResultBlocking() as SearchEngineResult.Error).e
        assertEquals("Negative or zero radius: -100.0", exception.message)
    }

     @Test
     fun testAddressesSearchZeroRadius() {
         loadOfflineData()

         val callback = BlockingOfflineSearchCallback()

         searchEngine.searchAddressesNearby(
             street = TEST_SEARCH_RESULT_MAPBOX.address?.street!!,
             proximity = TEST_SEARCH_RESULT_MAPBOX.coordinate,
             radiusMeters = 0.0,
             callback
         )

         assertTrue(callback.getResultBlocking() is SearchEngineResult.Error)

         val exception = (callback.getResultBlocking() as SearchEngineResult.Error).e
         assertEquals("Negative or zero radius: 0.0", exception.message)
     }

    @Test
    fun testAddressesInvalidProximity() {
        loadOfflineData()

        val validLons = arrayOf(-179.9, 0.0, 179.0)
        val validLats = arrayOf(-89.9, 0.0, 89.0)

        val invalidLons = arrayOf(-180.1, -180.0, 180.0, 180.1)
        val invalidLats = arrayOf(-90.1, -90.0, 90.0, 90.1)

        for (lon in validLons) {
            for (lat in validLats) {
                val callback = BlockingOfflineSearchCallback()

                searchEngine.searchAddressesNearby(
                    street = TEST_SEARCH_RESULT_MAPBOX.address?.street!!,
                    proximity = Point.fromLngLat(lon, lat),
                    radiusMeters = 1000.0,
                    callback
                )

                assertTrue("Lon=$lon, lat=$lat", callback.getResultBlocking() is SearchEngineResult.Results)
            }
        }

        for (lon in invalidLons) {
            for (lat in validLats) {
                val callback = BlockingOfflineSearchCallback()

                searchEngine.searchAddressesNearby(
                    street = TEST_SEARCH_RESULT_MAPBOX.address?.street!!,
                    proximity = Point.fromLngLat(lon, lat),
                    radiusMeters = 1000.0,
                    callback
                )

                assertTrue("Lon=$lon, lat=$lat", callback.getResultBlocking() is SearchEngineResult.Error)
                val exception = (callback.getResultBlocking() as SearchEngineResult.Error).e
                assertEquals("Invalid proximity(lon=$lon,lat=$lat)", exception.message)
            }
        }

        for (lon in validLons) {
            for (lat in invalidLats) {
                val callback = BlockingOfflineSearchCallback()

                searchEngine.searchAddressesNearby(
                    street = TEST_SEARCH_RESULT_MAPBOX.address?.street!!,
                    proximity = Point.fromLngLat(lon, lat),
                    radiusMeters = 1000.0,
                    callback
                )

                assertTrue("Lon=$lon, lat=$lat", callback.getResultBlocking() is SearchEngineResult.Error)
                val exception = (callback.getResultBlocking() as SearchEngineResult.Error).e
                assertEquals("Invalid proximity(lon=$lon,lat=$lat)", exception.message)
            }
        }
    }

    @Test
    fun testBoundingBox() {
        loadOfflineData()

        val lon = TEST_SEARCH_RESULT_MAPBOX.coordinate.longitude()
        val lat = TEST_SEARCH_RESULT_MAPBOX.coordinate.latitude()

        val bbox = BoundingBox.fromPoints(
            Point.fromLngLat(
                lon - 0.005,
                lat - 0.005,
            ),
            Point.fromLngLat(
                lon + 0.005,
                lat + 0.005,
            )
        )

        var inside = 0
        var outside = 0

        run {
            val searchResult = searchEngine.searchBlocking(
                TEST_QUERY,
                OfflineSearchOptions(
                    limit = 250,
                ),
            )

            assertTrue(searchResult is SearchEngineResult.Results)
            val results = searchResult.requireResults()
            assertTrue(results.isNotEmpty())

            for (res in results) {
                if (bbox.contains(res.coordinate)) {
                    inside++
                } else {
                    outside++
                }
            }

            // We must have both inside and outside results to perform a test
            assertTrue(inside > 0)
            assertTrue(outside > 0)
        }

        val searchResult = searchEngine.searchBlocking(
            TEST_QUERY,
            OfflineSearchOptions(
                limit = 250,
                boundingBox = bbox
            ),
        )

        assertTrue(searchResult is SearchEngineResult.Results)
        val results = searchResult.requireResults()
        assertEquals("All points inside a bbox must be preserved", inside, results.size)

        for (res in results) {
            val p = res.coordinate
            assertTrue("Point(lon=${p.longitude()},lat=${p.latitude()}) must be in bbox `$bbox`", bbox.contains(p))
        }
    }

    private companion object {

        private val tileStore: TileStore = TileStore.create()

        private const val LOG_TAG = "OfflineSearchEngineIntegrationTest"

        // TODO (SWEB-1113)
        // const val TEST_QUERY = "2011 15th Street Northwest, Washington, District of Columbia"
        const val TEST_QUERY = "2011 15th Street Northwest, Washington"

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
                    // See SWEB-1113 for more history
                    region = createCoreSearchAddressRegion("District of Columbia"),
                    country = createCoreSearchAddressCountry("United States")
                )
            ),
            // See SWEB-1113 for more history
            descriptionAddress = "2011 15th Street Northwest, District of Columbia, United States",

            distanceMeters = 0.0,
            center = Point.fromLngLat(-77.03404491238354, 38.9179071535832),
            serverIndex = null,
        )

        // assumes an accuracy of approximately 10 meters
        const val DOUBLE_COMPARISON_EPS = 0.0001

        fun Double.approximatelyEquals(other: Double) = abs(this - other) < DOUBLE_COMPARISON_EPS

        fun Point?.approximatelyEquals(other: Point?): Boolean {
            if (this == other) return true
            if (this == null || other == null) return false
            return latitude().approximatelyEquals(other.latitude()) && longitude().approximatelyEquals(other.longitude())
        }

        fun compareDistanceMeters(distance1: Double?, distance2: Double?): Boolean {
            if (distance1 == distance2) return true
            if (distance1 == null || distance2 == null) return false
            return abs(distance1 - distance2) < 10.0
        }

        fun BoundingBox.contains(p: Point): Boolean {
            val lon = p.longitude()
            val lat = p.latitude()
            return west() < lon && lon < east() && south() < lat && lat < north()
        }

        fun assertSearchResultEquals(expected: OfflineSearchResult, actual: OfflineSearchResult): Boolean {
            assertEquals(expected.name, actual.name)
            assertEquals(expected.descriptionText, actual.descriptionText)
            assertEquals(expected.address, actual.address)
            assertEquals(expected.routablePoints, actual.routablePoints)
            assertEquals(expected.type, actual.type)

            if (expected.coordinate.approximatelyEquals(actual.coordinate).not()) {
                Log.d(LOG_TAG, "assertSearchResultEquals coordinate: expected = ${expected.coordinate}, actual = ${actual.coordinate}")
                return false
            }

            if (compareDistanceMeters(expected.distanceMeters, actual.distanceMeters).not()) {
                Log.d(LOG_TAG, "assertSearchResultEquals distanceMeters: expected = ${expected.distanceMeters}, actual = ${actual.distanceMeters}")
                return false
            }

            return true
        }

        private val TEST_SEARCH_RESULT_MAPBOX = OfflineSearchResult(TEST_CORE_SEARCH_RESULT.mapToBase())

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
