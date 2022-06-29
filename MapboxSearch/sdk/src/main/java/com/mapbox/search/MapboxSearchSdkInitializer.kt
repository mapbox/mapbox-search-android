package com.mapbox.search

import android.content.Context
import androidx.startup.Initializer
import com.mapbox.common.MapboxSDKCommonInitializer
import com.mapbox.common.core.module.CommonSingletonModuleProvider

internal class MapboxSearchSdkInitializer : Initializer<MapboxSearchSdk> {

    override fun create(context: Context): MapboxSearchSdk {
        CommonSingletonModuleProvider.loaderInstance.load(SEARCH_SDK_NATIVE_LIBRARY_NAME)
        return MapboxSearchSdk
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(MapboxSDKCommonInitializer::class.java)
    }

    private companion object {
        private const val SEARCH_SDK_NATIVE_LIBRARY_NAME = "SearchCore"
    }
}
