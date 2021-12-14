package com.mapbox.search.tests_support

import com.mapbox.search.OfflineIndexChangeEvent
import com.mapbox.search.OfflineIndexErrorEvent
import com.mapbox.search.OfflineSearchEngine
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class BlockingOnIndexChangeListener(
    private val numberOfResults: Int,
) : OfflineSearchEngine.OnIndexChangeListener {

    private val results = mutableListOf<OnIndexChangeResult>()
    private var countDownLatch = CountDownLatch(1)

    override fun onIndexChange(event: OfflineIndexChangeEvent) {
        notifyEventsChanged(OnIndexChangeResult.Result(event))
    }

    override fun onError(event: OfflineIndexErrorEvent) {
        notifyEventsChanged(OnIndexChangeResult.Error(event))
    }

    private fun notifyEventsChanged(result: OnIndexChangeResult) {
        when (result) {
            is OnIndexChangeResult.Result -> {
                results.add(result)

                if (results.size >= numberOfResults) {
                    countDownLatch.countDown()
                }
            }
            is OnIndexChangeResult.Error -> {
                results.add(result)
                countDownLatch.countDown()
            }
        }
    }

    fun getResultsBlocking(
        timeout: Long = Long.MAX_VALUE,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): List<OnIndexChangeResult> {
        countDownLatch.await(timeout, timeUnit)
        return results
    }

    fun reset() {
        countDownLatch = CountDownLatch(1)
        results.clear()
    }

    sealed class OnIndexChangeResult {
        data class Result(val event: OfflineIndexChangeEvent) : OnIndexChangeResult()
        data class Error(val event: OfflineIndexErrorEvent) : OnIndexChangeResult()
    }
}
