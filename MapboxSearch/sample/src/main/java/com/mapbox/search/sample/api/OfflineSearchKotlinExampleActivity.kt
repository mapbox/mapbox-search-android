package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchOptions
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.sample.R

@OptIn(MapboxExperimental::class)
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
                    doSearch()
                } else {
                    logI("SearchApiExample", "Tiles loading error: ${result.error}")
                }
            }
        )
    }

    private fun doSearch() {
        logI("SearchApiExample", "Searching...")
        searchRequestTask?.cancel()
        searchRequestTask = searchEngine.search(
            "2011 15th Street Northwest, Washington, District of Columbia",
            OfflineSearchOptions(),
            searchCallback
        )
    }

    override fun onDestroy() {
        searchEngine.removeEngineReadyCallback(engineReadyCallback)
        tilesLoadingTask?.cancel()
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
