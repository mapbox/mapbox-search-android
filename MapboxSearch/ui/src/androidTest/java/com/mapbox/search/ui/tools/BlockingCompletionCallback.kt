package com.mapbox.search.ui.tools

import com.mapbox.search.common.CompletionCallback
import java.util.concurrent.CountDownLatch

internal class BlockingCompletionCallback<T> : CompletionCallback<T> {

    private var countDownLatch = CountDownLatch(1)
    private lateinit var completionCallbackResult: CompletionCallbackResult<T>

    override fun onComplete(result: T) {
        completionCallbackResult = CompletionCallbackResult.Result(result)
        countDownLatch.countDown()
    }

    override fun onError(e: Exception) {
        completionCallbackResult = CompletionCallbackResult.Error(e)
        countDownLatch.countDown()
    }

    fun getResultBlocking(): CompletionCallbackResult<T> {
        countDownLatch.await()
        return completionCallbackResult
    }

    fun reset() {
        countDownLatch = CountDownLatch(1)
    }

    sealed class CompletionCallbackResult<T> {
        data class Result<T>(val result: T?) : CompletionCallbackResult<T>()
        data class Error<T>(val e: Exception) : CompletionCallbackResult<T>()
    }
}
