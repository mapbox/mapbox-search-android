package com.mapbox.search.core.http

import com.mapbox.search.core.CoreHttpCallback
import com.mapbox.search.utils.SyncLocker
import java.util.concurrent.Executor

internal data class AsyncHttpCallbackDecorator(
    private val executor: Executor,
    private val syncLocker: SyncLocker,
    private val originalCallback: CoreHttpCallback,
) : CoreHttpCallback {

    override fun run(httpBody: String, responseCode: Int) {
        executor.execute {
            syncLocker.executeInSync {
                originalCallback.run(httpBody, responseCode)
            }
        }
    }
}
