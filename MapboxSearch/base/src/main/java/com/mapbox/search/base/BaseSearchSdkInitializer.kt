package com.mapbox.search.base

import android.content.Context
import androidx.startup.Initializer
import com.mapbox.common.MapboxSDKCommonInitializer
import com.mapbox.common.core.module.CommonSingletonModuleProvider

class BaseSearchSdkInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        appContext = context.applicationContext
        CommonSingletonModuleProvider.loaderInstance.load(SEARCH_SDK_NATIVE_LIBRARY_NAME)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(MapboxSDKCommonInitializer::class.java)
    }

    companion object {
        private const val SEARCH_SDK_NATIVE_LIBRARY_NAME = "SearchCore"

        lateinit var appContext: Context
    }
}
