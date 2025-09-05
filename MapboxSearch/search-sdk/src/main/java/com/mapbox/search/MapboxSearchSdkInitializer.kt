package com.mapbox.search

import com.mapbox.common.BaseMapboxInitializer

internal class MapboxSearchSdkInitializer : BaseMapboxInitializer<MapboxSearchSdk>() {
    override val initializerClass = MapboxSearchSdkInitializerImpl::class.java
}
