package com.mapbox.search.analytics

import com.mapbox.search.common.logger.loge
import com.mapbox.search.utils.AppInfoProvider
import com.mapbox.search.utils.TimeProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/mobile.crash.js
internal class CrashEventsFactory(
    private val timeProvider: TimeProvider,
    private val appInfoProvider: AppInfoProvider,
) {

    fun createEvent(throwable: Throwable, isSilent: Boolean, customData: Map<String, String>?): String {
        return JSONObject().apply {
            putSafe("event", CRASH_EVENT_NAME)
            putSafe("created", DATE_FORMAT.format(timeProvider.currentTimeMillis()))
            putSafe("sdkIdentifier", appInfoProvider.searchSdkPackageName)
            putSafe("sdkVersion", appInfoProvider.searchSdkVersionName)
            putSafe("osVersion", "Android-${appInfoProvider.osVersion}")
            putSafe("model", appInfoProvider.deviceModel)
            putSafe("device", appInfoProvider.deviceName)
            putSafe("appId", appInfoProvider.appPackageName)
            putSafe("appVersion", appInfoProvider.appVersion)
            putSafe("isSilent", java.lang.Boolean.toString(isSilent))
            putSafe("stackTraceHash", createStackTraceHash(throwable))
            putSafe("stackTrace", createStackTraceElement(throwable))
            if (!customData.isNullOrEmpty()) {
                putSafe("customData", createCustomDataElement(customData))
            }
        }.toString()
    }

    fun isAllowedForAnalytics(throwable: Throwable): Boolean = throwable.stackTrace.any { isAllowedStacktraceElement(it) }

    private fun isAllowedStacktraceElement(stackTraceElement: StackTraceElement): Boolean {
        return stackTraceElement.className.startsWith(appInfoProvider.searchSdkPackageName)
    }

    private fun JSONObject.putSafe(key: String, value: Any?) {
        try {
            if (value == null) {
                put(key, "null")
            } else {
                put(key, value)
            }
        } catch (e: JSONException) {
            loge("Failed json encode value: $value: ${e.message}")
        }
    }

    private fun createCustomDataElement(customData: Map<String, String>): JSONArray? {
        return try {
            val jsonArray = JSONArray()
            for ((key, value) in customData) {
                val keyValueObject = JSONObject()
                keyValueObject.put("name", key)
                keyValueObject.put("value", value)
                jsonArray.put(keyValueObject)
            }
            jsonArray
        } catch (e: JSONException) {
            loge("Failed to create JSON array for custom data: ${e.message}")
            null
        }
    }

    private fun createStackTraceElement(throwable: Throwable): String {
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

    private fun createStackTraceHash(throwable: Throwable): String {
        val result = StringBuilder()
        for (element in throwable.stackTrace) {
            result.append(element.className)
            result.append(element.methodName)
        }
        return Integer.toHexString(result.toString().hashCode())
    }

    private companion object {
        const val CRASH_EVENT_NAME = "mobile.crash"
        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    }
}
