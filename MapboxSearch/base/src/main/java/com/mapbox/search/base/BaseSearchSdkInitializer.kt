package com.mapbox.search.base

import com.mapbox.common.BaseMapboxInitializer

class BaseSearchSdkInitializer : BaseMapboxInitializer<Unit>() {
    override val initializerClass = BaseSearchSdkInitializerImpl::class.java
}
