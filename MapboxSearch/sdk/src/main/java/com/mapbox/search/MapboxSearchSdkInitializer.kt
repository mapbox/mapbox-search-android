package com.mapbox.search

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.mapbox.search.base.BaseSearchSdkInitializer

internal class MapboxSearchSdkInitializer : Initializer<MapboxSearchSdk> {
    override fun create(context: Context): MapboxSearchSdk {
        MapboxSearchSdk.initializeInternal(context.applicationContext as Application)
        return MapboxSearchSdk
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(BaseSearchSdkInitializer::class.java)
    }
}
