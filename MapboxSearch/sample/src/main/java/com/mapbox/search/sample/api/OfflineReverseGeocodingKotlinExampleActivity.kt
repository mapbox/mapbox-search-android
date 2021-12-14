package com.mapbox.search.sample.api

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineReverseGeoOptions
import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.result.SearchResult

class OfflineReverseGeocodingKotlinExampleActivity : Activity() {

    private lateinit var searchEngine: OfflineSearchEngine
    private lateinit var tilesLoadingTask: Cancelable
    private var searchRequestTask: SearchRequestTask? = null

    private val engineReadyCallback = object : OfflineSearchEngine.EngineReadyCallback {
        override fun onEngineReady() {
            Log.i("SearchApiExample", "Engine is ready")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Error during engine initialization", e)
        }
    }

    private val searchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            Log.i("SearchApiExample", "Results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchEngine = MapboxSearchSdk.getOfflineSearchEngine()
        searchEngine.addEngineReadyCallback(engineReadyCallback)

        val tileStore = searchEngine.tileStore

        val dcLocation = Point.fromLngLat(-77.0339911055176, 38.899920004207516)

        val descriptors = listOf(searchEngine.createTilesetDescriptor())

        val tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(dcLocation)
            .acceptExpired(true)
            .build()

        Log.i("SearchApiExample", "Loading tiles...")

        tilesLoadingTask = tileStore.loadTileRegion(
            "Washington DC",
            tileRegionLoadOptions,
            { progress -> Log.i("SearchApiExample", "Loading progress: $progress") },
            { region ->
                if (region.isValue) {
                    Log.i("SearchApiExample", "Tiles successfully loaded")
                    searchRequestTask = searchEngine.reverseGeocoding(
                        OfflineReverseGeoOptions(center = dcLocation),
                        searchCallback
                    )
                } else {
                    Log.i("SearchApiExample", "Tiles loading error: ${region.error}")
                }
            }
        )
    }

    override fun onDestroy() {
        searchEngine.removeEngineReadyCallback(engineReadyCallback)
        tilesLoadingTask.cancel()
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
