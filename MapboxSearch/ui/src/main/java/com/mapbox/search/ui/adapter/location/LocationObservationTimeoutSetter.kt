package com.mapbox.search.ui.adapter.location

import com.mapbox.search.base.location.LocationObservationProperties

/**
 * Sets location observation timeout for LocationEngineAdapter
 * @param timeout in mls, if null - timeout is not used
 */
public fun setLocationObservationTimeout(timeout: Long?) {
    LocationObservationProperties.locationObservationTimeout = timeout
}
