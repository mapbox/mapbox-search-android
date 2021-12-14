package com.mapbox.search.tests_support

import com.mapbox.search.DataProviderInitializationCallback
import com.mapbox.search.record.IndexableDataProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class BlockingDataProviderInitializationCallback(
    expectedProvidersAmount: Int
) : DataProviderInitializationCallback {

    private var countDownLatch = CountDownLatch(expectedProvidersAmount)
    private var initializationCallbackResult = InitializationResult.empty()

    override fun onInitialized(dataProvider: IndexableDataProvider<*>) {
        initializationCallbackResult = initializationCallbackResult.addInitialized(dataProvider)
        countDownLatch.countDown()
    }

    override fun onError(dataProvider: IndexableDataProvider<*>, e: Exception) {
        initializationCallbackResult = initializationCallbackResult.addError(dataProvider, e)
        countDownLatch.countDown()
    }

    fun getResultBlocking(timeout: Long = 10L, timeUnit: TimeUnit = TimeUnit.SECONDS): InitializationResult {
        if (!countDownLatch.await(timeout, timeUnit)) {
            throw TimeoutException()
        }
        return initializationCallbackResult
    }

    data class InitializationResult(val providersStatusMap: Map<String, Result<Unit>>) {

        fun addInitialized(dataProvider: IndexableDataProvider<*>) = InitializationResult(
            providersStatusMap = providersStatusMap + (dataProvider.dataProviderName to Result.success(Unit))
        )

        fun addError(dataProvider: IndexableDataProvider<*>, e: Exception) = InitializationResult(
            providersStatusMap = providersStatusMap + (dataProvider.dataProviderName to Result.failure(e))
        )

        companion object {

            fun empty() = InitializationResult(emptyMap())

            fun allInitialized(vararg dataProviders: IndexableDataProvider<*>) = InitializationResult(
                providersStatusMap = dataProviders.map { it.dataProviderName to Result.success(Unit) }.toMap()
            )
        }
    }
}
