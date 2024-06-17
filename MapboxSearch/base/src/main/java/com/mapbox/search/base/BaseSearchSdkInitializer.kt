package com.mapbox.search.base

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.mapbox.common.BaseMapboxInitializer
import com.mapbox.common.MapboxSDKCommonInitializer
import com.mapbox.common.SdkInfoRegistryFactory
import com.mapbox.common.SdkInformation
import com.mapbox.common.core.module.CommonSingletonModuleProvider
import com.mapbox.search.base.utils.UserAgentProvider

class BaseSearchSdkInitializer : BaseMapboxInitializer<Unit>() {
    override val initializerClass = BaseSearchSdkInitializerImpl::class.java
}
