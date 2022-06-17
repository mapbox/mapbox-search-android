package com.mapbox.search.sample.tools

import android.util.Log
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException

class MockWebServerRule : TestWatcher() {

    lateinit var mockServer: MockWebServer

    override fun starting(description: Description) {
        mockServer = MockWebServer()
        mockServer.start(DEFAULT_PORT)
    }

    override fun finished(description: Description) {
        try {
            mockServer.shutdown()
        } catch (e: IOException) {
            // If we use mocked response with large body delay,
            // MockWebServer may fail to await TaskRunner clean up.
            // We will be able to create new MockWebServer instance later on,
            // but some resources may still be uncleaned.
            Log.e("MockWebServerRule", "Error during MockWebServer shutdown", e)
        }
    }

    companion object {
        const val DEFAULT_PORT = 8081
    }
}
