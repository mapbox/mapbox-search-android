package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.AttributeSet
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.details.DetailsApi
import com.mapbox.search.details.DetailsApiSettings
import com.mapbox.search.details.RetrieveDetailsOptions
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.R

@OptIn(MapboxExperimental::class)
class DetailsApiKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_details_api_kt_example

    private lateinit var detailsApi: DetailsApi
    private var task: AsyncOperationTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        detailsApi = DetailsApi.create(DetailsApiSettings())
    }

    override fun onDestroy() {
        task?.cancel()
        super.onDestroy()
    }

    override fun startExample() {
        task = detailsApi.retrieveDetails(
            mapboxIds = listOf(
                "dXJuOm1ieHBvaTowZGY2MzE4Yi0wNGNjLTRkOTYtYTZmMy0yNmJmM2ZiODUyODU",
                "dXJuOm1ieHBvaTozMTRlYTY3ZS1kMjA4LTRlMTQtODMzZi1mYjE5NjZjNzFhYWI"
            ),
            options = RetrieveDetailsOptions(attributeSets = AttributeSet.values().toList()),
            callback = object : SearchCallback {
                override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                    logI("SearchApiExample", "Retrieve result:", results)
                    onFinished()
                }

                override fun onError(e: Exception) {
                    logE("SearchApiExample", "Retrieve error", e)
                    onFinished()
                }
            }
        )
    }
}
