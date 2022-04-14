package com.mapbox.search.tests_support

import com.mapbox.search.record.IndexableDataProviderEngine
import com.mapbox.search.record.LocalDataProvider
import java.util.concurrent.CountDownLatch

internal class BlockingOnDataProviderEngineRegisterListener(
    numberOfResults: Int
) : LocalDataProvider.OnDataProviderEngineRegisterListener {

    private val result: MutableList<Result> = mutableListOf()
    private var countDownLatch = CountDownLatch(numberOfResults)

    override fun onEngineRegistered(engine: IndexableDataProviderEngine) {
        result.add(Result.Success(engine))
        countDownLatch.countDown()
    }

    override fun onEngineRegistrationError(e: Exception) {
        result.add(Result.Error(e))
        countDownLatch.countDown()
    }

    fun getResultBlocking(): List<Result> {
        countDownLatch.await()
        return result
    }

    fun reset() {
        result.clear()
        countDownLatch = CountDownLatch(1)
    }

    sealed class Result {
        data class Success(val engine: IndexableDataProviderEngine) : Result()
        data class Error(val e: Exception) : Result()
    }
}
