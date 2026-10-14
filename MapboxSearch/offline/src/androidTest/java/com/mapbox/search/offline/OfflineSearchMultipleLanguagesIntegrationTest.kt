package com.mapbox.search.offline

import android.util.Log
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.offline.tests_support.BlockingEngineReadyCallback
import com.mapbox.search.offline.tests_support.BlockingOfflineSearchCallback.SearchEngineResult
import com.mapbox.search.offline.tests_support.getAllTileRegionsBlocking
import com.mapbox.search.offline.tests_support.loadTileRegionBlocking
import com.mapbox.search.offline.tests_support.removeTileRegionBlocking
import com.mapbox.search.offline.tests_support.searchBlocking
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

@OptIn(MapboxExperimental::class)
internal class OfflineSearchMultipleLanguagesIntegrationTest {

    private lateinit var searchEngine: OfflineSearchEngine

    @Before
    fun setUp() {
        searchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(tileStore = tileStore)
        )

        val engineReadyCallback = BlockingEngineReadyCallback()
        searchEngine.addEngineReadyCallback(engineReadyCallback)
        engineReadyCallback.getResultBlocking()
    }

    @Test
    fun testCountryNameIsLocalized() {
        listOf("Belgium", "Belgique").forEach { query ->
            assertFirstResultName(TILESET_EN, query, NewOfflineSearchResultType.COUNTRY, "Belgium")
            assertFirstResultName(TILESET_FR, query, NewOfflineSearchResultType.COUNTRY, "Belgique")
        }
    }

    @Test
    fun testRegionNameIsLocalized() {
        listOf("Normandy", "Normandie").forEach { query ->
            assertFirstResultName(TILESET_EN, query, NewOfflineSearchResultType.REGION, "Normandy")
            assertFirstResultName(TILESET_FR, query, NewOfflineSearchResultType.REGION, "Normandie")
        }
    }

    @Test
    fun testSwitchingBetweenLanguages() {
        assertFirstResultName(TILESET_EN, "Belgium", NewOfflineSearchResultType.COUNTRY, "Belgium")
        assertFirstResultName(TILESET_FR, "Belgium", NewOfflineSearchResultType.COUNTRY, "Belgique")
        assertFirstResultName(TILESET_EN, "Belgium", NewOfflineSearchResultType.COUNTRY, "Belgium")
    }

    @Test
    fun testAddressIsFoundInEachLanguage() {
        listOf(TILESET_EN, TILESET_FR).forEach { tileset ->
            searchEngine.selectTileset(tileset)

            val searchResult = searchEngine.searchBlocking(ADDRESS_QUERY, OfflineSearchOptions(origin = PARIS))
            assertTrue("Expected results but was: $searchResult", searchResult is SearchEngineResult.Results)

            val results = searchResult.requireResults()
            Log.d(LOG_TAG, "search($tileset, $ADDRESS_QUERY): ${results.map { "${it.name} (${it.newType})" }}")

            val address = results.firstOrNull { it.newType == NewOfflineSearchResultType.ADDRESS }
            assertEquals("Unexpected address in $tileset, results: $results", "7", address?.address?.houseNumber)
        }
    }

    /**
     * Searches [query] in [tileset] and checks the name of the first result of [type].
     */
    private fun assertFirstResultName(
        tileset: TilesetParameters,
        query: String,
        type: String,
        expectedName: String,
    ) {
        searchEngine.selectTileset(tileset)

        val searchResult = searchEngine.searchBlocking(query, OfflineSearchOptions(origin = PARIS))
        assertTrue("Expected results but was: $searchResult", searchResult is SearchEngineResult.Results)

        val results = searchResult.requireResults()
        val formattedResults = results.map { "${it.name} (${it.newType})" }
        Log.d(LOG_TAG, "search($tileset, $query): $formattedResults")

        val result = results.firstOrNull { it.newType == type }
        assertEquals(
            "Unexpected $type for $query in $tileset, results: $formattedResults",
            expectedName,
            result?.name,
        )
    }

    private companion object {

        const val LOG_TAG = "OfflineSearchMultipleLanguagesIntegrationTest"

        const val TEST_GROUP_ID = "paris-multiple-languages"

        val PARIS: Point = Point.fromLngLat(2.29320926999218, 48.859064444081085)

        const val ADDRESS_QUERY = "7 Av. de la Bourdonnais"

        val tileStore: TileStore = TileStore.create()

        val TILESET_EN = TilesetParameters.Builder()
            .language(IsoLanguageCode.ENGLISH)
            .build()

        val TILESET_FR = TilesetParameters.Builder()
            .language(IsoLanguageCode.FRENCH)
            .build()

        @BeforeClass
        @JvmStatic
        fun loadOfflineData() {
            removeAllTileRegions()

            val loadOptions = TileRegionLoadOptions.Builder()
                .descriptors(
                    listOf(
                        OfflineSearchEngine.createTilesetDescriptor(
                            listOf(TILESET_EN, TILESET_FR)
                        )
                    )
                )
                .geometry(PARIS)
                .acceptExpired(true)
                .build()

            val loadTilesResult = tileStore.loadTileRegionBlocking(TEST_GROUP_ID, loadOptions)
            assertTrue("Expected loaded region but was: $loadTilesResult", loadTilesResult.isValue)

            val tileRegion = requireNotNull(loadTilesResult.value)
            assertTrue(tileRegion.completedResourceCount > 0)
            assertEquals(tileRegion.requiredResourceCount, tileRegion.completedResourceCount)
        }

        @AfterClass
        @JvmStatic
        fun removeAllTileRegions() {
            tileStore.getAllTileRegionsBlocking().value?.forEach {
                tileStore.removeTileRegionBlocking(it.id)
            }
        }
    }
}
