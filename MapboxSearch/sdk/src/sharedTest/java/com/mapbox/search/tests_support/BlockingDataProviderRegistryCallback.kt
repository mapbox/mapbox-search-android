package com.mapbox.search.tests_support

import com.mapbox.search.IndexableDataProvidersRegistry
import java.util.concurrent.CountDownLatch

internal class BlockingDataProviderRegistryCallback : IndexableDataProvidersRegistry.Callback {

    private var countDownLatch = CountDownLatch(1)
    private lateinit var completionCallbackResult: RegistryCallbackResult

    override fun onSuccess() {
        completionCallbackResult = RegistryCallbackResult.Success
        countDownLatch.countDown()
    }

    override fun onError(e: Exception) {
        completionCallbackResult = RegistryCallbackResult.Error(e)
        countDownLatch.countDown()
    }

    fun getResultBlocking(): RegistryCallbackResult {
        countDownLatch.await()
        return completionCallbackResult
    }

    fun reset() {
        countDownLatch = CountDownLatch(1)
    }

    sealed class RegistryCallbackResult {
        object Success : RegistryCallbackResult()
        data class Error(val e: Exception) : RegistryCallbackResult()
    }
}
