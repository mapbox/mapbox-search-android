package com.mapbox.search.sample.extensions

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

fun MockWebServer.enqueue(firstResponse: MockResponse, vararg responses: MockResponse) {
    enqueue(firstResponse)
    responses.forEach { mockResponse ->
        enqueue(mockResponse)
    }
}
