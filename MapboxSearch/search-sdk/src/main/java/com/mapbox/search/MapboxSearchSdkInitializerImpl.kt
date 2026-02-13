package com.mapbox.search

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.BaseSearchSdkInitializerImpl
import com.mapbox.search.base.perf.PerformanceTracker

@OptIn(MapboxExperimental::class)
internal class MapboxSearchSdkInitializerImpl : Initializer<MapboxSearchSdk> {
    override fun create(context: Context): MapboxSearchSdk {
        PerformanceTracker.trackPerformanceSync("MapboxSearchSdkInitializerImpl#create") {
            MapboxSearchSdk.initialize(context.applicationContext as Application)
        }
        return MapboxSearchSdk
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(BaseSearchSdkInitializerImpl::class.java)
    }
}
