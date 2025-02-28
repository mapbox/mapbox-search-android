package com.mapbox.search.sample.api

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.Cancelable
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchOptions
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.sample.R
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(MapboxExperimental::class)
class OfflineMultipleLanguagesActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_offline_multiple_languages_kt_example

    private lateinit var searchEngine: OfflineSearchEngine
    private var tilesLoadingTask: Cancelable? = null

    private val tileStore = TileStore.create()
    private lateinit var tileRegionLoadOptions: TileRegionLoadOptions
    private val tileRegionId = "Paris - multiple languages"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchEngine = OfflineSearchEngine.create(OfflineSearchEngineSettings(tileStore))

        val descriptors = listOf(
            OfflineSearchEngine.createTilesetDescriptor(
                dataset = TILES_DATASET,
                version = TILES_VERSION,
                language = IsoLanguageCode.ENGLISH.code,
            ),
            OfflineSearchEngine.createTilesetDescriptor(
                dataset = TILES_DATASET,
                version = TILES_VERSION,
                language = IsoLanguageCode.FRENCH.code,
            )
        )

        tileRegionLoadOptions = TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(Point.fromLngLat(2.29320926999218, 48.859064444081085))
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

                    lifecycleScope.launchWhenStarted {
                        search()
                    }
                } else {
                    logI("SearchApiExample", "Tiles loading error: ${result.error}")
                }
            }
        )
    }

    private suspend fun search() {
        logI("SearchApiExample", "Searching in English...")
        searchEngine.selectTileset(TILES_DATASET, TILES_VERSION, IsoLanguageCode.ENGLISH)
        val resultsInEnglish = searchEngine.search("7 Av. de la Bourdonnais")
        logI("SearchApiExample", "Search results in English", resultsInEnglish)

        logI("SearchApiExample", "Searching in French...")
        searchEngine.selectTileset(TILES_DATASET, TILES_VERSION, IsoLanguageCode.FRENCH)
        val resultsInFrench = searchEngine.search("7 Av. de la Bourdonnais")
        logI("SearchApiExample", "Search results in French", resultsInFrench)
    }

    override fun onDestroy() {
        tilesLoadingTask?.cancel()
        super.onDestroy()
    }

    private companion object {

        const val TILES_DATASET = "mbx-gen2"
        const val TILES_VERSION = ""

        suspend fun OfflineSearchEngine.search(
            query: String,
            options: OfflineSearchOptions = OfflineSearchOptions(),
        ): Expected<Exception, List<OfflineSearchResult>> {
            return suspendCancellableCoroutine { continuation ->
                val task = search(query, options, object : OfflineSearchCallback {
                    override fun onResults(
                        results: List<OfflineSearchResult>,
                        responseInfo: OfflineResponseInfo
                    ) {
                        continuation.resumeWith(Result.success(ExpectedFactory.createValue(results)))
                    }

                    override fun onError(e: Exception) {
                        continuation.resumeWith(Result.success(ExpectedFactory.createError(e)))
                    }
                })

                continuation.invokeOnCancellation {
                    task.cancel()
                }
            }
        }
    }
}
