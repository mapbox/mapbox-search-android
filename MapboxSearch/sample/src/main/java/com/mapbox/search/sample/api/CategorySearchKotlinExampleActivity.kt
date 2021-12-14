package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.search.CategorySearchEngine
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.result.SearchResult

class CategorySearchKotlinExampleActivity : AppCompatActivity() {

    private lateinit var categorySearchEngine: CategorySearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

    private val searchCallback: SearchCallback = object : SearchCallback {

        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {
                Log.i("SearchApiExample", "No category search results")
            } else {
                Log.i("SearchApiExample", "Category search results: $results")
            }
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categorySearchEngine = MapboxSearchSdk.getCategorySearchEngine()

        searchRequestTask = categorySearchEngine.search(
            "cafe",
            CategorySearchOptions(limit = 1),
            searchCallback
        )
    }

    override fun onDestroy() {
        searchRequestTask.cancel()
        super.onDestroy()
    }
}
