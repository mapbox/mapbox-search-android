package com.mapbox.search.offline.tests_support

import com.mapbox.search.common.tests.BaseBlockingCallback
import com.mapbox.search.offline.OfflineSearchEngine

internal class BlockingEngineReadyCallback : BaseBlockingCallback<Unit>(), OfflineSearchEngine.EngineReadyCallback {
    override fun onEngineReady() {
        publishResult(Unit)
    }
}
