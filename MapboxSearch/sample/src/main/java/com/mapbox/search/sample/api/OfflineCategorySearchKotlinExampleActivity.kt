package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.offline.OfflineCategorySearchOptions
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.sample.R

@OptIn(MapboxExperimental::class)
class OfflineCategorySearchKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_offline_category_search_kt_example

    private lateinit var searchEngine: OfflineSearchEngine
    private var tilesLoadingTask: Cancelable? = null
    private var searchRequestTask: AsyncOperationTask? = null

    private val tileStore = TileStore.create()
    private lateinit var tileRegionLoadOptions: TileRegionLoadOptions
    private val tileRegionId = "Washington DC - category search"

    private val searchCallback = object : OfflineSearchCallback {
        override fun onResults(
            results: List<OfflineSearchResult>,
            responseInfo: OfflineResponseInfo
        ) {
            printMessage(results)
            onFinished()
        }

        override fun onError(e: Exception) {
            printMessage("Search error: $e")
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
        printMessage("Loading tiles...")

        tilesLoadingTask = tileStore.loadTileRegion(
            tileRegionId,
            tileRegionLoadOptions,
            { progress -> printMessage("Loading progress: $progress") },
            { result ->
                if (result.isValue) {
                    printMessage("Tiles successfully loaded: ${result.value}")
                    doSearch()
                } else {
                    printMessage("Tiles loading error: ${result.error}")
                }
            }
        )
    }

    private fun doSearch() {
        searchRequestTask = searchEngine.categorySearch(
            listOf("cafe", "park"),
            OfflineCategorySearchOptions(
                ensureResultsPerCategory = true,
            ),
            searchCallback
        )
    }

    override fun onDestroy() {
        tilesLoadingTask?.cancel()
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
