package com.mapbox.search.sample.api

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.offline.OfflineIndexChangeEvent
import com.mapbox.search.offline.OfflineIndexChangeEvent.EventType
import com.mapbox.search.offline.OfflineIndexErrorEvent
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchOptions
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.sample.BuildConfig

class OfflineSearchKotlinExampleActivity : Activity() {

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
            Log.i("SearchApiExample", "Search results: $results")
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
