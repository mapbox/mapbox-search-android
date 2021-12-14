package com.mapbox.search.tests_support

import com.mapbox.search.OfflineSearchEngine

internal class BlockingEngineReadyCallback :
    BaseBlockingCallback<BlockingEngineReadyCallback.Result>(),
    OfflineSearchEngine.EngineReadyCallback {

    override fun onEngineReady() {
        publishResult(Result.Ready)
    }

    override fun onError(e: Exception) {
        publishResult(Result.Error(e))
    }

    sealed class Result {
        object Ready : Result()
        data class Error(val e: Exception) : Result()
    }
}
