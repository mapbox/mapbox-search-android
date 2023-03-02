package com.mapbox.search

import com.google.gson.JsonSyntaxException
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.tests_support.BlockingOnDataProviderEngineRegisterListener
import com.mapbox.search.tests_support.createSearchEngineWithBuiltInDataProvidersBlocking
import com.mapbox.search.tests_support.record.clearBlocking
import com.mapbox.search.utils.loader.DataLoader
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
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

        MapboxSearchSdk.initialize(targetApplication)

        val searchEngineSettings = SearchEngineSettings(
            accessToken = DEFAULT_TEST_ACCESS_TOKEN,
            locationEngine = FixedPointLocationEngine(DEFAULT_TEST_USER_LOCATION),
            singleBoxSearchBaseUrl = MockWebServer().url("").toString()
        )

        searchEngine = createSearchEngineWithBuiltInDataProvidersBlocking(ApiType.SBS, searchEngineSettings)

        historyDataProvider = ServiceProvider.INSTANCE.historyDataProvider()
        historyDataProvider.clearBlocking()

        favoritesDataProvider = ServiceProvider.INSTANCE.favoritesDataProvider()
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

    @Test
    fun testSearchHistoryLoadingError() {
        MapboxSearchSdk.initialize(
            application = targetApplication,
            dataLoader = WrongDataFormatDataLoader
        )

        historyDataProvider = ServiceProvider.INSTANCE.historyDataProvider()
        val callback = BlockingCompletionCallback<List<HistoryRecord>>()
        historyDataProvider.getAll(callback)

        val result = callback.getResultBlocking()
        assertTrue(result.isError)

        val error = result.requireError()
        assertEquals(
            "java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path \$",
            error.message
        )
        assertTrue(error is JsonSyntaxException)
    }

    @Test
    fun testFavoritesLoadingError() {
        MapboxSearchSdk.initialize(
            application = targetApplication,
            dataLoader = WrongDataFormatDataLoader
        )

        favoritesDataProvider = ServiceProvider.INSTANCE.favoritesDataProvider()
        val callback = BlockingCompletionCallback<List<FavoriteRecord>>()
        favoritesDataProvider.getAll(callback)

        val result = callback.getResultBlocking()
        assertTrue(result.isError)

        val error = result.requireError()
        assertEquals(
            "java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path \$",
            error.message
        )
        assertTrue(error is JsonSyntaxException)
    }

    private object WrongDataFormatDataLoader : DataLoader<ByteArray> {
        override fun load(relativeDir: String, fileName: String): ByteArray {
            val text = when (fileName) {
                "search_history.bin" -> "wrong search history data format"
                "favorites.bin" -> "wrong favorites data format"
                else -> error("Unknown file name: $fileName")
            }
            return text.toByteArray()
        }

        override fun save(relativeDir: String, fileName: String, data: ByteArray) {
            // do nothing
        }
    }
}
