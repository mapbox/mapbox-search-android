package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.ev.EvConnectorType
import com.mapbox.search.offline.OfflineEvSearchOptions
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
import java.net.URI

@OptIn(MapboxExperimental::class)
class OfflineEvSearchKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_offline_ev_search_kt_example

    private lateinit var searchEngine: OfflineSearchEngine
    private var tilesLoadingTask: Cancelable? = null
    private var searchRequestTask: AsyncOperationTask? = null

    private val searchCallback = object : OfflineSearchCallback {

        override fun onResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
            printMessage("Results:\n${results.joinToString(separator = "\n") { it.toPrettyString() }}")
            onFinished()
        }

        override fun onError(e: Exception) {
            printMessage("Search error: $e")
            onFinished()
        }
    }

    private val onIndexChangeListener = object : OfflineSearchEngine.OnIndexChangeListener {
        override fun onIndexChange(event: OfflineIndexChangeEvent) {
            if (event.regionId == tileRegionId && (event.type == EventType.ADD || event.type == EventType.UPDATE)) {
                printMessage("$tileRegionId was successfully added or updated")
                startSearch()
            }
        }

        override fun onError(event: OfflineIndexErrorEvent) {
            printMessage("Offline index error: $event")
            onFinished()
        }
    }

    private val datasetCong = "experimental-poi-cat-alias" to "v65"

    private val tileRegionId = "Germany $datasetCong"
    private val tileStore = TileStore.create()
    private val descriptors = listOf(
        OfflineSearchEngine.createTilesetDescriptor(
            dataset = datasetCong.first,
            version = datasetCong.second,
        )
    )

    private val location = Point.fromLngLat(13.38391974336776, 52.516864797034486)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(
                tileStore = tileStore,
                tilesBaseUri = URI.create("https://search-sdk-offline-staging.tilestream.net")
            )
        )
    }

    override fun startExample() {
        searchEngine.addOnIndexChangeListener(onIndexChangeListener)

        val tileRegionLoadOptions = TileRegionLoadOptions
            .Builder()
            .descriptors(descriptors)
            .geometry(location)
            .acceptExpired(true)
            .build()

        tilesLoadingTask = tileStore.loadTileRegion(
            tileRegionId,
            tileRegionLoadOptions,
            { progress ->
                printMessage("Loading progress: $progress")
            },
            { result ->
                if (result.isValue) {
                    printMessage("Tiles successfully loaded: ${result.value}")
                } else {
                    printMessage("Tiles loading error: ${result.error}")
                }
            }
        )
    }

    private fun startSearch() {
        searchEngine.selectTileset(datasetCong.first, datasetCong.second)

        val evSearchOptions = OfflineEvSearchOptions(
            connectorTypes = listOf(
                EvConnectorType.TESLA_S,
                EvConnectorType.TESLA_R,
                EvConnectorType.IEC_62196_T1,
                EvConnectorType.IEC_62196_T2,
                EvConnectorType.IEC_62196_T3_A,
                EvConnectorType.IEC_62196_T3_C,
            )
        )

        val options = OfflineSearchOptions.Builder()
            .evSearchOptions(evSearchOptions)
            .proximity(location)
            .origin(location)
            .build()

        searchRequestTask = searchEngine.search(
            "charging station",
            options,
            searchCallback
        )
    }

    override fun onDestroy() {
        searchEngine.removeOnIndexChangeListener(onIndexChangeListener)
        tilesLoadingTask?.cancel()
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
