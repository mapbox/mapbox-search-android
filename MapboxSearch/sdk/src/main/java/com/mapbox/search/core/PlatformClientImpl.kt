package com.mapbox.search.core

import com.mapbox.search.analytics.InternalAnalyticsService
import com.mapbox.search.common.logger.DEFAULT_SEARCH_SDK_LOG_TAG
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.logger.loge
import com.mapbox.search.common.logger.logi
import com.mapbox.search.common.logger.logw
import com.mapbox.search.core.http.HttpClient
import com.mapbox.search.utils.UUIDProvider
import com.mapbox.search.utils.concurrent.MainThreadWorker
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker

internal class PlatformClientImpl(
    private val httpClient: HttpClient,
    private val analyticsService: InternalAnalyticsService,
    private val uuidProvider: UUIDProvider,
    private val mainThreadWorker: MainThreadWorker = SearchSdkMainThreadWorker,
    private val callbackDecorator: (CoreHttpCallback) -> CoreHttpCallback = { it },
) : CorePlatformClient {

    override fun httpRequest(url: String, body: ByteArray?, requestID: Int, sessionID: String, callback: CoreHttpCallback) {
        val decoratedCallback = callbackDecorator(callback)

        if (body == null) {
            httpClient.httpGet(url, requestID, sessionID, decoratedCallback)
        } else {
            httpClient.httpPost(url, body, requestID, sessionID, decoratedCallback)
        }
    }

    override fun log(level: CoreLogLevel, message: String) {
        when (level) {
            CoreLogLevel.DEBUG -> logd(tag = DEFAULT_SEARCH_SDK_LOG_TAG, message = message)
            CoreLogLevel.INFO -> logi(tag = DEFAULT_SEARCH_SDK_LOG_TAG, message = message)
            CoreLogLevel.WARNING -> logw(tag = DEFAULT_SEARCH_SDK_LOG_TAG, message = message)
            CoreLogLevel.ERROR -> loge(tag = DEFAULT_SEARCH_SDK_LOG_TAG, message = message)
        }
    }

    override fun generateUUID(): String {
        return uuidProvider.generateUUID()
    }

    override fun postEvent(json: String) {
        analyticsService.postJsonEvent(json)
    }

    override fun scheduleTask(function: CoreTaskFunction, delayMS: Int) {
        mainThreadWorker.postDelayed(delayMS.toLong()) {
            function.run()
        }
    }
}
