package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.tests_support.BlockingDataProviderInitializationCallback
import com.mapbox.search.tests_support.BlockingDataProviderInitializationCallback.InitializationResult
import com.mapbox.search.tests_support.DelayedDataLoader
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

internal class MapboxSearchSdkTest : BaseTest() {

    @Test
    fun testAddProviderInitializationCallbackBeforeInit() {
        val blockingCallback = BlockingDataProviderInitializationCallback(DEFAULT_DATA_PROVIDERS_AMOUNT)
        MapboxSearchSdk.addDataProviderInitializationCallback(blockingCallback)

        MapboxSearchSdk.initializeInternal(
            application = targetApplication,
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            allowReinitialization = true,
        )

        assertEquals(
            InitializationResult.allInitialized(
                MapboxSearchSdk.serviceProvider.historyDataProvider(),
                MapboxSearchSdk.serviceProvider.favoritesDataProvider(),
            ),
            blockingCallback.getResultBlocking(),
        )
    }

    @Test
    fun testAddProviderInitializationCallbackAfterInit() {
        MapboxSearchSdk.initializeInternal(
            application = targetApplication,
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            allowReinitialization = true,
        )

        val blockingCallback = BlockingDataProviderInitializationCallback(DEFAULT_DATA_PROVIDERS_AMOUNT)
        MapboxSearchSdk.addDataProviderInitializationCallback(blockingCallback)

        assertEquals(
            InitializationResult.allInitialized(
                MapboxSearchSdk.serviceProvider.historyDataProvider(),
                MapboxSearchSdk.serviceProvider.favoritesDataProvider(),
            ),
            blockingCallback.getResultBlocking(),
        )

        val anotherBlockingCallback = BlockingDataProviderInitializationCallback(DEFAULT_DATA_PROVIDERS_AMOUNT)
        MapboxSearchSdk.addDataProviderInitializationCallback(anotherBlockingCallback)

        assertEquals(
            InitializationResult.allInitialized(
                MapboxSearchSdk.serviceProvider.historyDataProvider(),
                MapboxSearchSdk.serviceProvider.favoritesDataProvider(),
            ),
            anotherBlockingCallback.getResultBlocking(timeout = 0),
        )
    }

    @Test
    fun testRemoveProviderInitializationCallback() {
        MapboxSearchSdk.initializeInternal(
            application = targetApplication,
            accessToken = TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION),
            allowReinitialization = true,
            dataLoader = DelayedDataLoader(delayInMillis = TEST_DATA_LOADER_DELAY_MILLIS),
        )

        val blockingCallback = object : DataProviderInitializationCallback {
            @Volatile var triggered = false

            override fun onInitialized(dataProvider: IndexableDataProvider<*>) {
                triggered = true
            }

            override fun onError(dataProvider: IndexableDataProvider<*>, e: Exception) {
                triggered = true
            }
        }

        MapboxSearchSdk.addDataProviderInitializationCallback(blockingCallback)
        MapboxSearchSdk.removeDataProviderInitializationCallback(blockingCallback)

        val anotherBlockingCallback = BlockingDataProviderInitializationCallback(DEFAULT_DATA_PROVIDERS_AMOUNT)
        MapboxSearchSdk.addDataProviderInitializationCallback(anotherBlockingCallback)
        anotherBlockingCallback.getResultBlocking()

        assertFalse(blockingCallback.triggered)
    }

    @After
    override fun tearDown() {
        MapboxSearchSdk.resetDataProviderInitializationCallbacks()
    }

    companion object {
        const val DEFAULT_DATA_PROVIDERS_AMOUNT = 2
        const val TEST_ACCESS_TOKEN = "pk.test"
        const val TEST_DATA_LOADER_DELAY_MILLIS = 1000L
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)
    }
}
