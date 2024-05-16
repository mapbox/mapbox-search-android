package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.sample.R

class ForwardGeocodingKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_forward_geocoding_kt_example

    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: AsyncOperationTask? = null

    private val searchCallback = object : SearchSelectionCallback {

        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            if (suggestions.isEmpty()) {
                logI("SearchApiExample", "No suggestions found")
                onFinished()
            } else {
                logI("SearchApiExample", "Search suggestions: ${prettify(suggestions)}.\n\n\nSelecting first suggestion...")
                searchRequestTask = searchEngine.select(suggestions.first(), this)
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            logI("SearchApiExample", "Search result:", result)
            onFinished()
        }

        override fun onResults(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            logI("SearchApiExample", "Category search results:", results)
            onFinished()
        }

        override fun onError(e: Exception) {
            logI("SearchApiExample", "Search error", e)
            onFinished()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(getString(R.string.mapbox_access_token))
        )
    }

    override fun startExample() {
        searchRequestTask = searchEngine.search(
            "Paris Eiffel Tower",
            SearchOptions(limit = 5),
            searchCallback
        )
    }

    override fun onDestroy() {
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
