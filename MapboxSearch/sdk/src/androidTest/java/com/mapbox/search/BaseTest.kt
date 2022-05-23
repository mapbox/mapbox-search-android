package com.mapbox.search

import android.app.Application
import androidx.annotation.CallSuper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapbox.geojson.Point
import com.mapbox.search.common.BuildConfig
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
internal abstract class BaseTest {

    protected val targetApplication: Application
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

    protected fun readBytesFromAssets(fileName: String): ByteArray {
        return targetApplication.resources.assets.open(fileName).use {
            it.readBytes()
        }
    }

    protected fun readFileFromAssets(fileName: String): String = String(readBytesFromAssets(fileName))

    protected fun createSuccessfulResponse(bodyContentPath: String): MockResponse {
        return MockResponse()
            .setResponseCode(200)
            .setBody(readFileFromAssets(bodyContentPath))
    }

    @Before
    @CallSuper
    open fun setUp() {}

    @After
    @CallSuper
    open fun tearDown() {}

    protected companion object {

        const val DEFAULT_TEST_ACCESS_TOKEN = "pk.test"
        val DEFAULT_TEST_USER_LOCATION: Point = Point.fromLngLat(10.1, 11.1234567)

        val TESTING_USER_AGENT: String
            get() = if (BuildConfig.DEBUG) {
                "search-sdk-android-internal/${BuildConfig.VERSION_NAME}"
            } else {
                "search-sdk-android/${BuildConfig.VERSION_NAME}"
            }

        fun Double.format(digits: Int) = "%.${digits}f".format(Locale.ENGLISH, this)

        fun Double.formatToBackendConvention() = format(6)

        fun formatPoints(vararg points: Point?): String {
            return points
                .flatMap { listOfNotNull(it?.longitude(), it?.latitude()) }
                .joinToString(separator = ",") { it.formatToBackendConvention() }
        }
    }
}
