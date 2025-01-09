package com.mapbox.search.extensions.utils

import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.result.SearchResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun SearchEngine.categorySearch(
    canonicalNames: List<String>,
    options: CategorySearchOptions
): Result<Pair<List<SearchResult>, ResponseInfo>> {
    return suspendCancellableCoroutine {
        val task = search(canonicalNames, options, object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                it.resume(Result.success(results to responseInfo))
            }

            override fun onError(e: Exception) {
                it.resume(Result.failure(e))
            }
        })

        it.invokeOnCancellation {
            task.cancel()
        }
    }
}