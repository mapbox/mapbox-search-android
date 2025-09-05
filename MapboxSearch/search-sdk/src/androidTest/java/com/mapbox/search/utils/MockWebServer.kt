package com.mapbox.search.utils

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

internal fun MockWebServer.enqueueMultiple(response: MockResponse, times: Int) {
    repeat(times) {
        enqueue(response)
    }
}
