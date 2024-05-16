package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.sample.R

class JapanSearchKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_japan_search_kt_example

    private lateinit var searchEngine: SearchEngine
    private var searchRequestTask: AsyncOperationTask? = null

    private val searchCallback = object : SearchSelectionCallback {

        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            if (suggestions.isEmpty()) {
                logI("SearchApiExample", "No suggestions found")
                onFinished()
            } else {
                logI("SearchApiExample", "Search suggestions: ${prettify(suggestions)}.\nSelecting first suggestion...")
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

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            ApiType.SBS,
            SearchEngineSettings()
        )
    }

    override fun startExample() {
        searchRequestTask = searchEngine.search(
            "東京",
            SearchOptions(
                countries = listOf(IsoCountryCode.JAPAN),
                languages = listOf(IsoLanguageCode.JAPANESE),
            ),
            searchCallback
        )
    }

    override fun onDestroy() {
        searchRequestTask?.cancel()
        super.onDestroy()
    }
}
