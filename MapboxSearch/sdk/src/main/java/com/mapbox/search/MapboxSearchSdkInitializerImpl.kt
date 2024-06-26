package com.mapbox.search

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.mapbox.search.base.BaseSearchSdkInitializerImpl

internal class MapboxSearchSdkInitializerImpl : Initializer<MapboxSearchSdk> {
    override fun create(context: Context): MapboxSearchSdk {
        MapboxSearchSdk.initialize(context.applicationContext as Application)
        return MapboxSearchSdk
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(BaseSearchSdkInitializerImpl::class.java)
    }
}
