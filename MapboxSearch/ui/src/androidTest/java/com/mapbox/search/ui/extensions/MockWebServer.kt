package com.mapbox.search.ui.extensions

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

internal fun MockWebServer.enqueue(firstResponse: MockResponse, vararg responses: MockResponse) {
    enqueue(firstResponse)
    responses.forEach { mockResponse ->
        enqueue(mockResponse)
    }
}
