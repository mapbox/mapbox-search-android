package com.mapbox.search.sample

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.common.TileStore
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineSearchSettings
import com.mapbox.search.SearchSdkSettings

open class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        enableStrictMode()
        LeakCanaryConfiguration.apply()

        System.setProperty("com.mapbox.mapboxsearch.enableSBS", BuildConfig.ENABLE_SBS.toString())

        MapboxSearchSdk.initialize(
            application = this,
            accessToken = BuildConfig.MAPBOX_API_TOKEN,
            locationEngine = LocationEngineProvider.getBestLocationEngine(this),
            searchSdkSettings = SearchSdkSettings(maxHistoryRecordsAmount = 5),
            offlineSearchSettings = OfflineSearchSettings(tileStore = TileStore.create()),
        )
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

            // TODO(#611): For some reason on API 30 we can fail with LeakedClosableViolation.
            // Need to investigate it separately.
            detectLeakedClosableObjects()

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
}
