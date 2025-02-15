package com.mapbox.search.sample

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import com.mapbox.common.HttpRequest
import com.mapbox.common.HttpRequestOrResponse
import com.mapbox.common.HttpResponse
import com.mapbox.common.HttpServiceFactory
import com.mapbox.common.HttpServiceInterceptorInterface
import com.mapbox.common.HttpServiceInterceptorRequestContinuation
import com.mapbox.common.HttpServiceInterceptorResponseContinuation
import com.mapbox.common.LogConfiguration
import com.mapbox.common.LoggingLevel

open class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        enableDebugHttpLogs()
        enableStrictMode()
        LeakCanaryConfiguration.apply()
    }

    @SuppressLint("RestrictedApi")
    private fun enableDebugHttpLogs(onlySearchLogs: Boolean = true) {
        if (!BuildConfig.DEBUG) {
            return
        }

        LogConfiguration.setLoggingLevel(LoggingLevel.DEBUG)

        fun filter(url: String): Boolean {
            return !onlySearchLogs || SEARCH_ENDPOINTS_URL.any { url.startsWith(it) }
        }

        HttpServiceFactory.getInstance().setInterceptor(object : HttpServiceInterceptorInterface {
            override fun onRequest(request: HttpRequest, continuation: HttpServiceInterceptorRequestContinuation) {
                if (filter(request.url)) {
                    Log.i("SearchSdkHttp", "onRequest: $request")
                }
                continuation.run(HttpRequestOrResponse.valueOf(request))
            }

            override fun onResponse(response: HttpResponse, continuation: HttpServiceInterceptorResponseContinuation) {
                if (filter(response.request.url)) {
                    Log.i("SearchSdkHttp", "onResponse: $response")
                }
                continuation.run(response)
            }
        })
    }

    private fun enableStrictMode() {
        if (!BuildConfig.DEBUG) {
            return
        }

        with(ThreadPolicy.Builder()) {
            // We can't enable detectDiskReads() because of native .so library loading.
            // detectDiskReads()

            // Flag `testCoverageEnabled` from build.gradle :sdk module, when turned on,
            // transforms :sdk classes in a way, that first call to any of mentioned classes,
            // will trigger network request on caller side. It breaks our policy.
            // To overcome this issue we check, whether we're in coverage mode.
            if (!BuildConfig.COVERAGE_ENABLED) {
                // Fails with Maps SDK https://github.com/mapbox/mapbox-maps-android/issues/372
                // detectDiskWrites()
                detectNetwork()
            }

            penaltyLog()
            penaltyDeath()
            StrictMode.setThreadPolicy(build())
        }

        with(VmPolicy.Builder()) {
            // We can't enable all checks by detectAll() because it includes detectUntaggedSockets()
            // which fails when we work with OkHttp https://github.com/square/okhttp/issues/3537

            detectLeakedSqlLiteObjects()
            detectActivityLeaks()
            detectLeakedRegistrationObjects()
            detectFileUriExposure()

            // Fails because of an issue in the Common SDK.
            // detectLeakedClosableObjects()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                detectContentUriWithoutPermission()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                detectCredentialProtectedWhileLocked()
                detectImplicitDirectBoot()
            }

            penaltyLog()
            penaltyDeath()
            StrictMode.setVmPolicy(build())
        }
    }

    private companion object {
        val SEARCH_ENDPOINTS_URL = listOf(
            "https://api.mapbox.com/autofill",
            "https://api.mapbox.com/search",
            "https://api.mapbox.com/geocoding"
        )
    }
}
