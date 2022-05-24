package com.mapbox.search.analytics

import com.mapbox.search.utils.AppInfoProvider
import com.mapbox.search.utils.TimeProvider
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

internal class CrashEventsFactoryTest {

    private lateinit var timeProvider: TimeProvider
    private lateinit var appInfoProvider: AppInfoProvider

    private lateinit var crashEventsFactory: CrashEventsFactory

    private val testException = createTestException()

    @Before
    fun setUp() {
        timeProvider = TestTimeProvider()
        appInfoProvider = TestAppInfoProvider()

        crashEventsFactory = CrashEventsFactory(timeProvider, appInfoProvider)
    }

    @Test
    fun testGeneratedEvent() {
        val event = crashEventsFactory.createEvent(throwable = testException, isSilent = true, customData = null)

        val json = JSONObject(event)

        assertEquals("mobile.crash", json.getString("event"))
        assertEquals(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(LOCAL_TIME_MILLIS),
            json.getString("created")
        )
        assertEquals(appInfoProvider.searchSdkPackageName, json.getString("sdkIdentifier"))
        assertEquals(appInfoProvider.searchSdkVersionName, json.getString("sdkVersion"))
        assertEquals("Android-${appInfoProvider.osVersion}", json.getString("osVersion"))
        assertEquals(appInfoProvider.deviceModel, json.getString("model"))
        assertEquals(appInfoProvider.deviceName, json.getString("device"))
        assertEquals(appInfoProvider.appPackageName, json.getString("appId"))
        assertEquals(appInfoProvider.appVersion, json.getString("appVersion"))

        assertEquals("true", json.getString("isSilent"))
        assertEquals(expectedStackTraceHash(testException), json.getString("stackTraceHash"))
        assertEquals(expectedStackTraceElement(testException), json.getString("stackTrace"))
        assertTrue(
            runCatching {
                assertEquals(null, json.getString("customData"))
            }.exceptionOrNull() is JSONException
        )
    }

    @Test
    fun testFilledCustomData() {
        val event = crashEventsFactory.createEvent(
            throwable = testException,
            isSilent = true,
            customData = mapOf("sdk" to "Search SDK")
        )

        val json = JSONObject(event)

        assertEquals("[{\"name\":\"sdk\",\"value\":\"Search SDK\"}]", json.getString("customData"))
    }

    @Test(expected = JSONException::class)
    fun testEmptyCustomData() {
        val event = crashEventsFactory.createEvent(throwable = testException, isSilent = true, customData = null)
        val json = JSONObject(event)
        assertEquals(null, json.getString("customData"))
    }

    // This is a function that just returns an exception,
    // because we need at least one method of the SDK in the Exception stack trace
    private fun createTestException() = Exception("Test message")

    private class TestTimeProvider : TimeProvider {
        override fun currentTimeMillis() = LOCAL_TIME_MILLIS
    }

    private class TestAppInfoProvider : AppInfoProvider {
        override val searchSdkPackageName: String = SEARCH_SDK_PACKAGE_NAME
        override val searchSdkVersionName: String = SEARCH_SDK_VERSION_NAME
        override val deviceModel: String = DEVICE_MODEL
        override val deviceName: String = DEVICE_NAME
        override val osVersion: String = OS_VERSION
        override val appPackageName: String = APP_PACKAGE_NAME
        override val appVersion: String = APP_VERSION
    }

    private companion object {

        const val LOCAL_TIME_MILLIS = 12345L
        const val SEARCH_SDK_PACKAGE_NAME: String = "com.mapbox.search"
        const val SEARCH_SDK_VERSION_NAME: String = "test.1.2.3"
        const val DEVICE_MODEL: String = "Android SDK built for x86"
        const val DEVICE_NAME: String = "generic_x86"
        const val OS_VERSION: String = "123"
        const val APP_PACKAGE_NAME: String = "test.app.name"
        const val APP_VERSION: String = "test.3.1.5"

        fun expectedStackTraceHash(throwable: Throwable): String {
            val hashCode = throwable.stackTrace.joinToString("") {
                it.className + it.methodName
            }.hashCode()
            return Integer.toHexString(hashCode)
        }

        private fun expectedStackTraceElement(throwable: Throwable): String {
            val stackTraceElements = throwable.stackTrace

            val prefix = if (
                stackTraceElements.isNotEmpty() &&
                isAllowedStacktraceElement(stackTraceElements[0]) &&
                throwable.message != null
            ) {
                throwable.message
            } else {
                "***"
            }

            val trace = stackTraceElements.joinToString(separator = "\n", postfix = "\n") {
                if (isAllowedStacktraceElement(it)) {
                    "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
                } else {
                    "*"
                }
            }

            return "$prefix\n$trace"
        }

        fun isAllowedStacktraceElement(stackTraceElement: StackTraceElement): Boolean {
            return stackTraceElement.className.startsWith(SEARCH_SDK_PACKAGE_NAME)
        }
    }
}
