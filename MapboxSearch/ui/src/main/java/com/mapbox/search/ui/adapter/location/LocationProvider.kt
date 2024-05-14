package com.mapbox.search.ui.adapter.location

import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.internal.bindgen.LocationProvider

/**
 * Sets location observation timeout for LocationEngineAdapter
 * @param timeout in mls, if null - timeout is not used
 */
public fun LocationProvider.setLocationObservationTimeout(timeout: Long?) {

    when (this) {
        is WrapperLocationProvider -> this.getLocationProvider()?.setLocationObservationTimeout(timeout)
        is LocationEngineAdapter -> this.setObservationTimeout(timeout)
        else -> throw IllegalStateException("This class is not a LocationEngineAdapter nor WrapperLocationProvider")
    }
}
