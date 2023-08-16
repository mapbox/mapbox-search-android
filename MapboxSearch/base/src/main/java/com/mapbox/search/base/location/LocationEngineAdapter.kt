package com.mapbox.search.base.location

import android.annotation.SuppressLint
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.DeviceLocationProviderType
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationService
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreLocationProvider
import com.mapbox.search.base.logger.loge
import com.mapbox.search.base.utils.LocalTimeProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.extension.lastKnownLocationCommon
import com.mapbox.search.internal.bindgen.LonLatBBox

class LocationEngineAdapter(
    locationService: LocationService,
    private val timeProvider: TimeProvider = LocalTimeProvider()
) : CoreLocationProvider {

    private var locationProvider: DeviceLocationProvider? = null

    @Volatile
    private var lastLocationInfo: Location? = null

    private val locationObserver = LocationObserver { locations ->
        lastLocationInfo = locations.maxByOrNull { it.timestamp }
        if (lastLocationInfo != null) {
            stopLocationListener()
        }
    }

    init {
        val request = LocationProviderRequest.Builder()
            .accuracy(AccuracyLevel.PASSIVE)
            .build()

        locationService.getDeviceLocationProvider(DeviceLocationProviderType.BEST, request).onValue {
            locationProvider = it
        }.onError {
            locationProvider = null
        }

        locationService.lastKnownLocationCommon { result ->
            result.onValue { location ->
                lastLocationInfo = location
            }.onError {
                loge("Can't access last location: ${it.message}")
                startLocationListener()
            }
        }
    }

    private fun startLocationListener() {
        locationProvider?.addLocationObserver(locationObserver)
    }

    private fun stopLocationListener() {
        locationProvider?.removeLocationObserver(locationObserver)
    }

    override fun getLocation(): Point? {
        val location = lastLocationInfo
        return if (location == null) {
            startLocationListener()
            null
        } else {
            if (location.timestamp + LOCATION_CACHE_TIME_MS <= timeProvider.currentTimeMillis()) {
                startLocationListener()
            }
            location.toPoint()
        }
    }

    // Will be accessed from WrapperLocationProvider
    override fun getViewport(): LonLatBBox? {
        throw NotImplementedError()
    }

    private companion object {
        private const val LOCATION_CACHE_TIME_MS = 30_000L

        private fun Location.toPoint() = Point.fromLngLat(longitude, latitude)
    }
}
