package com.mapbox.search.sample.api

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineReverseGeoOptions
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.sample.BuildConfig

class OfflineReverseGeocodingKotlinExampleActivity : Activity() {

    private lateinit var searchEngine: OfflineSearchEngine
    private lateinit var tilesLoadingTask: Cancelable
    private var searchRequestTask: AsyncOperationTask? = null

    private val engineReadyCallback = object : OfflineSearchEngine.EngineReadyCallback {
        override fun onEngineReady() {
            Log.i("SearchApiExample", "Engine is ready")
        }
    }

    private val searchCallback = object : OfflineSearchCallback {

        override fun onResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
            Log.i("SearchApiExample", "Results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tileStore = TileStore.create()

        searchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(
                accessToken = BuildConfig.MAPBOX_API_TOKEN,
                tileStore = tileStore
            )
        )

        searchEngine.addEngineReadyCallback(engineReadyCallback)

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
