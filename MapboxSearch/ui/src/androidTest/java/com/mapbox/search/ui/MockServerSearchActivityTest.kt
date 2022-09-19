package com.mapbox.search.ui

import android.content.Context
import androidx.annotation.CallSuper
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.adevinta.android.barista.rule.cleardata.ClearFilesRule
import com.adevinta.android.barista.rule.flaky.FlakyTestRule
import com.mapbox.geojson.Point
import com.mapbox.search.ServiceProvider
import com.mapbox.search.ui.extensions.enqueue
import com.mapbox.search.ui.tools.BlockingCompletionCallback
import com.mapbox.search.ui.tools.MockWebServerRule
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
internal abstract class MockServerSearchActivityTest {

    var activityTestRule = ActivityScenarioRule(TestActivity::class.java)

    var grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant("android.permission.ACCESS_FINE_LOCATION")

    var clearFileRule = ClearFilesRule()

    var mockWebServerRule = MockWebServerRule()

    var flakyRule = FlakyTestRule().apply {
        allowFlakyAttemptsByDefault(DEFAULT_RETRY_ATTEMPTS_FOR_FAILED_TEST + 1)
    }

    @Rule
    @JvmField
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
        .around(activityTestRule)
        .around(grantPermissionRule)
        .around(clearFileRule)
        .around(mockWebServerRule)

    val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    val mockServer: MockWebServer
        get() = mockWebServerRule.mockServer

    @Before
    @CallSuper
    open fun beforeEachTest() {
        // Because MapboxSearchSdk is initialized once on app creation,
        // we need to reset its state before each test run.

        val callback = BlockingCompletionCallback<Unit>()
        ServiceProvider.INSTANCE.historyDataProvider().clear(callback)
        callback.getResultBlocking()

        callback.reset()
        ServiceProvider.INSTANCE.favoritesDataProvider().clear(callback)
        callback.getResultBlocking()
    }

    @After
    @CallSuper
    open fun afterEachTest() {
    }

    fun <T : Any> withTestActivity(block: TestActivity.() -> T?): T? {
        var result: T? = null
        activityTestRule.scenario.onActivity { activity ->
            result = activity.block()
        }
        return result
    }

    protected fun readBytesFromAssets(fileName: String): ByteArray {
        return InstrumentationRegistry.getInstrumentation().context.resources.assets.open(fileName).use {
            it.readBytes()
        }
    }

    protected fun readFileFromAssets(fileName: String): String = String(readBytesFromAssets(fileName))

    protected fun createSuccessfulResponse(bodyContentPath: String): MockResponse {
        return MockResponse()
            .setResponseCode(200)
            .setBody(readFileFromAssets(bodyContentPath))
    }

    protected fun createNoNetworkConnectionResponse(): MockResponse {
        return MockResponse()
            .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
    }

    protected fun MockWebServer.enqueueSuccessfulResponses(
        bodyContentPath: String,
        vararg bodyContentPaths: String
    ) {
        enqueue(
            createSuccessfulResponse(bodyContentPath),
            *bodyContentPaths.map(::createSuccessfulResponse).toTypedArray()
        )
    }

    protected companion object {

        private const val DEFAULT_RETRY_ATTEMPTS_FOR_FAILED_TEST = 2

        fun formatPointsToBackendConvention(vararg points: Point?): String {
            return points
                .flatMap { listOfNotNull(it?.longitude(), it?.latitude()) }
                .joinToString(separator = ",") { "%.${6}f".format(it) }
        }
    }
}
