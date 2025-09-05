package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchResultCallback
import com.mapbox.search.common.tests.BaseBlockingCallback
import com.mapbox.search.result.SearchResult

internal class BlockingSearchResultCallback :
    SearchResultCallback,
    BaseBlockingCallback<BlockingSearchResultCallback.Result>() {

    override fun onResult(result: SearchResult, responseInfo: ResponseInfo) {
        publishResult(Result.Success(result, responseInfo))
    }

    override fun onError(e: Exception) {
        publishResult(Result.Error(e))
    }

    sealed class Result {

        val isSuccess: Boolean
            get() = this is Success

        val isError: Boolean
            get() = !isSuccess

        fun getSuccess() = this as Success
        fun getError() = this as Error

        data class Success(val result: SearchResult, val responseInfo: ResponseInfo) : Result()
        data class Error(val e: Exception) : Result()
    }
}
