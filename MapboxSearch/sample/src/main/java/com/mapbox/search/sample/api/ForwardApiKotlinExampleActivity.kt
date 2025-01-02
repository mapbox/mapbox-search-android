package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.AttributeSet
import com.mapbox.search.ForwardSearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.R

class ForwardApiKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_forward_api_kt_example

    private lateinit var searchEngine: SearchEngine
    private var task: AsyncOperationTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        searchEngine = SearchEngine.createSearchEngine(ApiType.SEARCH_BOX, SearchEngineSettings())
    }

    override fun onDestroy() {
        task?.cancel()
        super.onDestroy()
    }

    override fun startExample() {
        val options = ForwardSearchOptions.Builder()
            .proximity(Point.fromLngLat(-77.03400742663874, 38.904863105693586))
            .attributeSets(
                listOf(
                    AttributeSet.BASIC,
                    AttributeSet.PHOTOS,
                    AttributeSet.VENUE,
                    AttributeSet.VISIT,
                )
            )
            .build()

        task = searchEngine.forward(
            query = "Starbucks",
            options = options,
            callback = object : SearchCallback {
                override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                    logI("SearchApiExample", "Forward request results:", results)
                    onFinished()
                }

                override fun onError(e: Exception) {
                    logE("SearchApiExample", "Forward request error", e)
                    onFinished()
                }
            }
        )
    }
}
