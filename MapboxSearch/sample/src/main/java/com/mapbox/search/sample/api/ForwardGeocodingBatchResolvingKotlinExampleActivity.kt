package com.mapbox.search.sample.api

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchMultipleSelectionCallback
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

class ForwardGeocodingBatchResolvingKotlinExampleActivity : Activity() {

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: AsyncOperationTask

    private val searchCallback = object : SearchSelectionCallback, SearchMultipleSelectionCallback {

        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            if (suggestions.isEmpty()) {
                Log.i("SearchApiExample", "No suggestions found")
            } else {
                Log.i("SearchApiExample", "Search suggestions: $suggestions.")
                searchRequestTask = searchEngine.select(suggestions, this)
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Search result: $result")
        }

        override fun onResults(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Category search results: $results")
        }

        override fun onResult(
            suggestions: List<SearchSuggestion>,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Batch retrieve results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings()
        )

        searchRequestTask = searchEngine.search(
            "Paris Eiffel Tower",
            SearchOptions(),
            searchCallback
        )
    }

    override fun onDestroy() {
        searchRequestTask.cancel()
        super.onDestroy()
    }
}
