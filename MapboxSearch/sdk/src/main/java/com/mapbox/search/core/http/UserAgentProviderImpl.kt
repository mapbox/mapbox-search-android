package com.mapbox.search.core.http

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.mapbox.search.common.BuildConfig
import com.mapbox.search.utils.extension.packageInfoOrNull
import com.mapbox.search.utils.extension.versionCodeCompat
import java.util.Locale

internal fun interface UserAgentProvider {
    fun userAgent(): String
}

internal class UserAgentProviderImpl(private val context: Context) : UserAgentProvider {

    private val userAgent: String by lazy {
        val hostAppName = when {
            // If application label is initialized from string resource,
            // then try to get string for english locale
            context.applicationInfo.labelRes != 0 -> {
                val config = Configuration(context.resources.configuration).apply {
                    setLocale(Locale.ENGLISH)
                }
                context.createConfigurationContext(config).getText(context.applicationInfo.labelRes)
            }
            // Otherwise, request default application label
            else -> context.packageManager.getApplicationLabel(context.applicationInfo)
        }

        val hostAppVersionName = context.packageInfoOrNull?.versionName ?: "Unknown"
        val hostAppPackage = context.packageName
        val hostAppVersionCode = context.packageInfoOrNull?.versionCodeCompat ?: "Unknown"

        val androidVersion = Build.VERSION.RELEASE

        val searchSdkVersionName = BuildConfig.VERSION_NAME

        val userAgent = "$hostAppName/$hostAppVersionName " +
                "($hostAppPackage; build:$hostAppVersionCode; Android $androidVersion) " +
                "MapboxSearchSDK-Android/$searchSdkVersionName"

        userAgent.encodeToValidHeaderValue()
    }

    override fun userAgent(): String = userAgent

    /**
     * OkHttp has internal check for HTTP Header values:
     * https://github.com/square/okhttp/blob/okhttp_4.9.x/okhttp/src/main/kotlin/okhttp3/Headers.kt#L450-L453
     */
    private fun String.encodeToValidHeaderValue(): String {
        return asSequence()
            .map { c ->
                if (c == '\t' || c in '\u0020'..'\u007e') {
                    c
                } else {
                    '_'
                }
            }
            .joinToString(separator = "")
    }
}
