package com.mapbox.search.core.http

import com.mapbox.search.core.CoreHttpCallback
import com.mapbox.search.utils.UUIDProvider
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

internal class HttpClientImpl(
    private val client: OkHttpClient,
    private val errorsCache: HttpErrorsCache,
    private val uuidProvider: UUIDProvider,
    private val userAgentProvider: UserAgentProvider
) : HttpClient {

    override fun httpGet(url: String, requestID: Int, sessionID: String, callback: CoreHttpCallback) {
        makeRequest(url, null, requestID, sessionID, callback)
    }

    override fun httpPost(url: String, body: ByteArray, requestID: Int, sessionID: String, callback: CoreHttpCallback) {
        makeRequest(url, body, requestID, sessionID, callback)
    }

    private fun makeRequest(url: String, body: ByteArray?, requestID: Int, sessionID: String, callback: CoreHttpCallback) {
        try {
            val request = with(Request.Builder()) {
                url(url)
                addHeader(HEADER_SESSION_ID, sessionID)
                addHeader(HEADER_REQUEST_ID, uuidProvider.generateUUID())
                addHeader(HEADER_USER_AGENT, userAgentProvider.userAgent())
                if (body != null) {
                    post(body.toRequestBody(MEDIA_TYPE_JSON))
                }
                build()
            }

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    errorsCache.put(requestID, e)
                    callback.run(e.message ?: HTTP_ERROR_MESSAGE, Int.MIN_VALUE)
                }

                override fun onResponse(call: Call, response: Response) {
                    val bodyText = response.body?.string() ?: ""
                    callback.run(bodyText, response.code)
                }
            })
        } catch (e: Exception) {
            errorsCache.put(requestID, e)
            callback.run(e.message ?: HTTP_ERROR_MESSAGE, Int.MIN_VALUE)
        }
    }

    private companion object {

        const val HTTP_ERROR_MESSAGE = "http error"
        const val HEADER_SESSION_ID = "X-MBX-SEARCH-SID"
        const val HEADER_REQUEST_ID = "X-Request-ID"
        const val HEADER_USER_AGENT = "User-Agent"

        val MEDIA_TYPE_JSON = "application/json".toMediaType()
    }
}
