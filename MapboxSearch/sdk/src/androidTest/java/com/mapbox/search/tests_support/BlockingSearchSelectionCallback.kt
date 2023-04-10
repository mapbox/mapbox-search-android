package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchMultipleSelectionCallback
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.common.tests.BaseBlockingCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

internal class BlockingSearchSelectionCallback :
    BaseBlockingCallback<BlockingSearchSelectionCallback.SearchEngineResult>(),
    SearchSelectionCallback,
    SearchMultipleSelectionCallback {

    override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
        publishResult(SearchEngineResult.Suggestions(suggestions, responseInfo))
    }

    override fun onError(e: Exception) {
        publishResult(SearchEngineResult.Error(e))
    }

    override fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo) {
        publishResult(SearchEngineResult.Result(result, responseInfo))
    }

    override fun onCategoryResult(
        suggestion: SearchSuggestion,
        results: List<SearchResult>,
        responseInfo: ResponseInfo
    ) {
        publishResult(SearchEngineResult.CategoryResult(results, responseInfo))
    }

    override fun onResult(
        suggestions: List<SearchSuggestion>,
        results: List<SearchResult>,
        responseInfo: ResponseInfo
    ) {
        publishResult(SearchEngineResult.BatchResult(results, responseInfo))
    }

    sealed class SearchEngineResult {

        fun requireSuggestions() = (this as Suggestions).suggestions

        fun requireResult() = (this as Result)

        fun requireError() = (this as Error).e

        data class Suggestions(val suggestions: List<SearchSuggestion>, val responseInfo: ResponseInfo) : SearchEngineResult()
        data class Result(val result: SearchResult, val responseInfo: ResponseInfo) : SearchEngineResult()
        data class CategoryResult(val results: List<SearchResult>, val responseInfo: ResponseInfo) : SearchEngineResult()
        data class BatchResult(val results: List<SearchResult>, val responseInfo: ResponseInfo) : SearchEngineResult()
        data class Error(val e: Exception) : SearchEngineResult()
    }
}
