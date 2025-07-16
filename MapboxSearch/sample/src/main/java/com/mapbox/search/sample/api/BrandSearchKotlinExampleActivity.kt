package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.BrandSearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.RestrictedMapboxSearchAPI
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.R

@OptIn(RestrictedMapboxSearchAPI::class)
class BrandSearchKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_brand_search_kt_example

    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: AsyncOperationTask? = null

    private val searchCallback: SearchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            printSearchResults(results)
            onFinished()
        }

        override fun onError(e: Exception) {
            printMessage("Brand search error: $e")
            onFinished()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            ApiType.SEARCH_BOX,
            SearchEngineSettings()
        )
    }

    override fun startExample() {
        val location = Point.fromLngLat(-77.03402630638045, 38.904839580620695)
        searchRequestTask = searchEngine.brandSearch(
            "starbucks",
            BrandSearchOptions(
                proximity = location,
            ),
            searchCallback
        )
    }

    override fun onDestroy() {
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
