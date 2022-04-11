package com.mapbox.search

import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.tests_support.BlockingOnDataProviderEngineRegisterListener
import com.mapbox.search.tests_support.record.clearBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class LocalDataProvidersIntegrationTest : BaseTest() {

    private lateinit var searchEngine: SearchEngine
    private lateinit var historyDataProvider: HistoryDataProvider
    private lateinit var favoritesDataProvider: FavoritesDataProvider

    @Before
    override fun setUp() {
        super.setUp()

        MapboxSearchSdk.initializeInternal(
            application = targetApplication,
            accessToken = DEFAULT_TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(DEFAULT_TEST_USER_LOCATION),
            allowReinitialization = true,
        )

        searchEngine = MapboxSearchSdk.createSearchEngine(ApiType.SBS, SearchEngineSettings(), useSharedCoreEngine = true)

        historyDataProvider = MapboxSearchSdk.serviceProvider.historyDataProvider()
        historyDataProvider.clearBlocking()

        favoritesDataProvider = MapboxSearchSdk.serviceProvider.favoritesDataProvider()
        favoritesDataProvider.clearBlocking()
    }

    @Test
    fun testHistoryDataProviderEngineRegisterListener() {
        val listener = BlockingOnDataProviderEngineRegisterListener(1)
        historyDataProvider.addOnDataProviderEngineRegisterListener(listener)
        assertTrue(listener.getResultBlocking().first() is BlockingOnDataProviderEngineRegisterListener.Result.Success)
    }

    @Test
    fun testFavoriteDataProviderEngineRegisterListener() {
        val listener = BlockingOnDataProviderEngineRegisterListener(1)
        favoritesDataProvider.addOnDataProviderEngineRegisterListener(listener)
        assertTrue(listener.getResultBlocking().first() is BlockingOnDataProviderEngineRegisterListener.Result.Success)
    }
}
