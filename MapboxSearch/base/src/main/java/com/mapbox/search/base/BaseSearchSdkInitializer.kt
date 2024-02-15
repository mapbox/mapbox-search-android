package com.mapbox.search.base

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.mapbox.common.MapboxSDKCommonInitializer
import com.mapbox.common.SdkInfoRegistryFactory
import com.mapbox.common.SdkInformation
import com.mapbox.common.core.module.CommonSingletonModuleProvider
import com.mapbox.search.base.utils.UserAgentProvider

class BaseSearchSdkInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        appContext = context.applicationContext
        CommonSingletonModuleProvider.loaderInstance.load(SEARCH_SDK_NATIVE_LIBRARY_NAME)

        sdkInformation = SdkInformation(
            UserAgentProvider.sdkName,
            UserAgentProvider.sdkVersionName,
            UserAgentProvider.sdkPackageName
        )

        SdkInfoRegistryFactory.getInstance().registerSdkInformation(sdkInformation)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(MapboxSDKCommonInitializer::class.java)
    }

    companion object {
        private const val SEARCH_SDK_NATIVE_LIBRARY_NAME = "SearchCore"

        lateinit var appContext: Context

        val app: Application
            get() = appContext as Application

        lateinit var sdkInformation: SdkInformation
            private set
    }
}
