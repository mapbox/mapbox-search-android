package com.mapbox.search.sample.api

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineIndexChangeEvent
import com.mapbox.search.OfflineIndexChangeEvent.EventType
import com.mapbox.search.OfflineIndexErrorEvent
import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.OfflineSearchEngineSettings
import com.mapbox.search.OfflineSearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.BuildConfig

class OfflineSearchKotlinExampleActivity : Activity() {

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
            Log.i("SearchApiExample", "Search results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tileStore = TileStore.create()

        searchEngine = MapboxSearchSdk.createOfflineSearchEngine(
            OfflineSearchEngineSettings(
                applicationContext = this,
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

        val tileRegionId = "Washington DC"

        searchEngine.addOnIndexChangeListener(object : OfflineSearchEngine.OnIndexChangeListener {
            override fun onIndexChange(event: OfflineIndexChangeEvent) {
                if (event.regionId == tileRegionId && (event.type == EventType.ADD || event.type == EventType.UPDATE)) {
                    Log.i("SearchApiExample", "$tileRegionId was successfully added or updated")

                    searchRequestTask = searchEngine.search(
                        "Cafe",
                        OfflineSearchOptions(),
                        searchCallback
                    )
                }
            }

            override fun onError(event: OfflineIndexErrorEvent) {
                Log.i("SearchApiExample", "Offline index error: $event")
            }
        })

        tilesLoadingTask = tileStore.loadTileRegion(
            tileRegionId,
            tileRegionLoadOptions,
            { progress -> Log.i("SearchApiExample", "Loading progress: $progress") },
            { result ->
                val printableResult = if (result.isValue) {
                    result.value
                } else {
                    result.error
                }
                Log.i("SearchApiExample", "$tileRegionId loading result: $printableResult")
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
