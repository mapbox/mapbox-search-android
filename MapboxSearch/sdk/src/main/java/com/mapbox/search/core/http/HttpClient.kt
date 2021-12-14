package com.mapbox.search.core.http

import com.mapbox.search.core.CoreHttpCallback

internal interface HttpClient {
    fun httpGet(url: String, requestID: Int, sessionID: String, callback: CoreHttpCallback)

    fun httpPost(url: String, body: ByteArray, requestID: Int, sessionID: String, callback: CoreHttpCallback)
}
