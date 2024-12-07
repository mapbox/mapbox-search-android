package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.search.ApiType
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.R

class CategorySearchKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_discover_kotlin_example

    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: AsyncOperationTask? = null

    private val searchCallback: SearchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {
                logI("SearchApiExample", "No category search results")
            } else {
                logI("SearchApiExample", "Category search results:", results)
            }
            onFinished()
        }

        override fun onError(e: Exception) {
            logI("SearchApiExample", "Search error", e)
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
        searchRequestTask = searchEngine.search(
            listOf("cafe", "coffee_shop", "hotel"),
            CategorySearchOptions(
                limit = 10,
                ensureResultsPerCategory = true,
            ),
            searchCallback
        )
    }

    override fun onDestroy() {
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
