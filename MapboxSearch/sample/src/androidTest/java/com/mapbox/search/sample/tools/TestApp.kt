package com.mapbox.search.sample.tools

import android.app.Application
import android.os.Build
import android.os.LocaleList
import com.mapbox.common.TileStore
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineSearchEngineSettings
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.FixedPointLocationEngine
import com.mapbox.search.sample.BuildConfig
import com.mapbox.search.sample.Constants.TEST_USER_LOCATION
import java.util.Locale

class TestApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Ensure distance formatting uses miles (not meters).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(Locale.US, Locale.ENGLISH)
            LocaleList.setDefault(localeList)
        } else {
            Locale.setDefault(Locale.US)
        }

        System.setProperty("com.mapbox.mapboxsearch.enableSBS", true.toString())

        // TODO FIXME
//        val locationEngine = FixedPointLocationEngine(TEST_USER_LOCATION)
//
//        MapboxSearchSdk.initialize(
//            searchEngineSettings = SearchEngineSettings(
//                applicationContext = this,
//                accessToken = BuildConfig.MAPBOX_API_TOKEN,
//                locationEngine = locationEngine,
//                singleBoxSearchBaseUrl = "http://localhost:${MockWebServerRule.DEFAULT_PORT}/"
//            ),
//            offlineSearchEngineSettings = OfflineSearchEngineSettings(
//                applicationContext = this,
//                accessToken = BuildConfig.MAPBOX_API_TOKEN,
//                tileStore = TileStore.create(),
//                locationEngine = locationEngine
//            ),
//        )
    }
}
