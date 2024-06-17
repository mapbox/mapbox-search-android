package com.mapbox.search.base

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.mapbox.common.SdkInfoRegistryFactory
import com.mapbox.common.SdkInformation
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.core.MapboxSearchCoreInitializerImpl

class BaseSearchSdkInitializerImpl : Initializer<Unit> {

    override fun create(context: Context) {
        appContext = context.applicationContext

        SdkInfoRegistryFactory.getInstance().registerSdkInformation(
            SdkInformation(
                UserAgentProvider.sdkName,
                UserAgentProvider.sdkVersionName,
                UserAgentProvider.sdkPackageName
            )
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(MapboxSearchCoreInitializerImpl::class.java)
    }

    companion object {
        lateinit var appContext: Context

        val app: Application
            get() = appContext as Application
    }
}
