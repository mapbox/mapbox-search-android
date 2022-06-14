package com.mapbox.search.autofill.ktx

import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchMultipleSelectionCallback
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.SelectOptions
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.suspendCancellableCoroutine

internal sealed class SearchSuggestionsResponse {

    data class Suggestions(
        val suggestions: List<SearchSuggestion>,
        val responseInfo: ResponseInfo,
    ) : SearchSuggestionsResponse()

    data class Error(val e: Exception) : SearchSuggestionsResponse()
}

internal sealed class SearchSelectionResponse {

    data class Suggestions(
        val suggestions: List<SearchSuggestion>,
        val responseInfo: ResponseInfo,
    ) : SearchSelectionResponse()

    data class Result(
        val suggestion: SearchSuggestion,
        val result: SearchResult,
        val responseInfo: ResponseInfo,
    ) : SearchSelectionResponse()

    data class CategoryResult(
        val suggestion: SearchSuggestion,
        val results: List<SearchResult>,
        val responseInfo: ResponseInfo,
    ) : SearchSelectionResponse()

    data class Error(val e: Exception) : SearchSelectionResponse()
}

internal sealed class SearchMultipleSelectionResponse {

    data class Results(
        val suggestions: List<SearchSuggestion>,
        val results: List<SearchResult>,
        val responseInfo: ResponseInfo,
    ) : SearchMultipleSelectionResponse()

    data class Error(val e: Exception) : SearchMultipleSelectionResponse()
}

internal sealed class SearchResultsResponse {

    data class Results(
        val results: List<SearchResult>,
        val responseInfo: ResponseInfo,
    ) : SearchResultsResponse()

    data class Error(val e: Exception) : SearchResultsResponse()
}

@JvmSynthetic
internal suspend fun SearchEngine.search(query: String, options: SearchOptions): SearchSuggestionsResponse {
    return suspendCancellableCoroutine { continuation ->
        val task = search(query, options, object : SearchSuggestionsCallback {
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                continuation.resumeWith(
                    Result.success(SearchSuggestionsResponse.Suggestions(suggestions, responseInfo))
                )
            }

            override fun onError(e: Exception) {
                continuation.resumeWith(Result.success(SearchSuggestionsResponse.Error(e)))
            }
        })

        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
}

@JvmSynthetic
internal suspend fun SearchEngine.search(options: ReverseGeoOptions): SearchResultsResponse {
    return suspendCancellableCoroutine { continuation ->
        val task = search(options, object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                continuation.resumeWith(
                    Result.success(SearchResultsResponse.Results(results, responseInfo))
                )
            }

            override fun onError(e: Exception) {
                continuation.resumeWith(
                    Result.success(SearchResultsResponse.Error(e))
                )
            }
        })

        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
}

@JvmSynthetic
internal suspend fun SearchEngine.select(
    suggestion: SearchSuggestion,
    options: SelectOptions = SelectOptions()
): SearchSelectionResponse {
    return suspendCancellableCoroutine { continuation ->
        val task = select(suggestion, options, object : SearchSelectionCallback {
            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                continuation.resumeWith(
                    Result.success(SearchSelectionResponse.Suggestions(suggestions, responseInfo))
                )
            }

            override fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo) {
                continuation.resumeWith(
                    Result.success(SearchSelectionResponse.Result(suggestion, result, responseInfo))
                )
            }

            override fun onCategoryResult(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                continuation.resumeWith(
                    Result.success(SearchSelectionResponse.CategoryResult(suggestion, results, responseInfo))
                )
            }

            override fun onError(e: Exception) {
                continuation.resumeWith(
                    Result.success(SearchSelectionResponse.Error(e))
                )
            }
        })

        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
}

@JvmSynthetic
internal suspend fun SearchEngine.select(
    suggestions: List<SearchSuggestion>,
): SearchMultipleSelectionResponse {
    return suspendCancellableCoroutine { continuation ->
        val task = select(suggestions, object : SearchMultipleSelectionCallback {

            override fun onResult(
                suggestions: List<SearchSuggestion>,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                continuation.resumeWith(
                    Result.success(SearchMultipleSelectionResponse.Results(suggestions, results, responseInfo))
                )
            }

            override fun onError(e: Exception) {
                continuation.resumeWith(
                    Result.success(SearchMultipleSelectionResponse.Error(e))
                )
            }
        })

        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
}
