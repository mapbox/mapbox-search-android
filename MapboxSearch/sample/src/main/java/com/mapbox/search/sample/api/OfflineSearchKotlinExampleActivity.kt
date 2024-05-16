package com.mapbox.search.sample.api

import android.os.Bundle
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
import com.mapbox.search.sample.R

class OfflineSearchKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_offline_search_kt_example

    private lateinit var searchEngine: OfflineSearchEngine
    private var tilesLoadingTask: Cancelable? = null
    private var searchRequestTask: AsyncOperationTask? = null

    private val tileStore = TileStore.create()
    private lateinit var tileRegionLoadOptions: TileRegionLoadOptions
    private val tileRegionId = "Washington DC"

    private val engineReadyCallback = object : OfflineSearchEngine.EngineReadyCallback {
        override fun onEngineReady() {
            logI("SearchApiExample", "Engine is ready")
        }
    }

    private val searchCallback = object : OfflineSearchCallback {

        override fun onResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
            logI("SearchApiExample", "Search results:", results)
            onFinished()
        }

        override fun onError(e: Exception) {
            logI("SearchApiExample", "Search error", e)
            onFinished()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(
                accessToken = getString(R.string.mapbox_access_token),
                tileStore = tileStore
            )
        )

        searchEngine.addEngineReadyCallback(engineReadyCallback)

        val descriptors = listOf(OfflineSearchEngine.createTilesetDescriptor())

        val dcLocation = Point.fromLngLat(-77.0339911055176, 38.899920004207516)

        tileRegionLoadOptions = TileRegionLoadOptions
            .Builder()
            .descriptors(descriptors)
            .geometry(dcLocation)
            .acceptExpired(true)
            .build()

        searchEngine.addOnIndexChangeListener(object : OfflineSearchEngine.OnIndexChangeListener {
            override fun onIndexChange(event: OfflineIndexChangeEvent) {
                if (event.regionId == tileRegionId && (event.type == EventType.ADD || event.type == EventType.UPDATE)) {
                    logI("SearchApiExample", "$tileRegionId was successfully added or updated")

                    searchRequestTask = searchEngine.search(
                        "2011 15th Street Northwest, Washington, District of Columbia",
                        OfflineSearchOptions(),
                        searchCallback
                    )
                }
            }

            override fun onError(event: OfflineIndexErrorEvent) {
                logI("SearchApiExample", "Offline index error: $event")
                onFinished()
            }
        })
    }

    override fun startExample() {
        logI("SearchApiExample", "Loading tiles...")
        tilesLoadingTask = tileStore.loadTileRegion(
            tileRegionId,
            tileRegionLoadOptions,
            { progress -> logI("SearchApiExample", "Loading progress: $progress") },
            { result ->
                if (result.isValue) {
                    logI("SearchApiExample", "Tiles successfully loaded: ${result.value}")
                } else {
                    logI("SearchApiExample", "Tiles loading error: ${result.error}")
                }
            }
        )
    }

    override fun onDestroy() {
        searchEngine.removeEngineReadyCallback(engineReadyCallback)
        tilesLoadingTask?.cancel()
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
