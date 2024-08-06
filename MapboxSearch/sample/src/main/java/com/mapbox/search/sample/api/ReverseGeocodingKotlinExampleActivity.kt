package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.R

class ReverseGeocodingKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_reverse_geocoding_kt_example

    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: AsyncOperationTask? = null

    private val searchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {
                logI("SearchApiExample", "No reverse geocoding results")
            } else {
                logI("SearchApiExample", "Reverse geocoding results:", results)
            }
            onFinished()
        }

        override fun onError(e: Exception) {
            logI("SearchApiExample", "Reverse geocoding error", e)
            onFinished()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings()
        )
    }

    override fun startExample() {
        val options = ReverseGeoOptions(
            center = Point.fromLngLat(2.294434, 48.858349),
            limit = 1
        )
        searchRequestTask = searchEngine.search(options, searchCallback)
    }

    override fun onDestroy() {
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
