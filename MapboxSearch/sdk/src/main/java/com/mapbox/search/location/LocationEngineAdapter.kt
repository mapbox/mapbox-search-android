package com.mapbox.search.location

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.search.common.extension.toPoint
import com.mapbox.search.common.logger.loge
import com.mapbox.search.common.logger.logi
import com.mapbox.search.core.CoreLocationProvider
import com.mapbox.search.internal.bindgen.LonLatBBox
import com.mapbox.search.utils.LocalTimeProvider
import com.mapbox.search.utils.TimeProvider

// Suppressed because we check permission but lint can't detekt it
@SuppressLint("MissingPermission")
internal class LocationEngineAdapter(
    private val app: Application,
    private val locationEngine: LocationEngine,
    private val timeProvider: TimeProvider = LocalTimeProvider()
) : CoreLocationProvider {

    @Volatile
    private var lastLocationInfo = LocationInfo(null, 0)

    private val locationEngineCallback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            result?.lastLocation?.let {
                lastLocationInfo = LocationInfo(it.toPoint(), timeProvider.currentTimeMillis())
            }
            stopLocationListener()
        }

        override fun onFailure(e: Exception) {
            loge(e, "Can't access location")
        }
    }

    init {
        if (!PermissionsManager.areLocationPermissionsGranted(app)) {
            logi("Location permission is not granted")
        } else {
            locationEngine.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
                override fun onSuccess(result: LocationEngineResult?) {
                    val location = result?.lastLocation
                    if (location != null) {
                        lastLocationInfo = LocationInfo(location.toPoint(), timeProvider.currentTimeMillis())
                    } else {
                        startLocationListener()
                    }
                }

                override fun onFailure(e: Exception) {
                    loge(e, "Can't access last location")
                    startLocationListener()
                }
            })
        }
    }

    private fun startLocationListener() {
        try {
            val request = LocationEngineRequest.Builder(DEFAULT_MIN_TIME_MS)
                .setDisplacement(DEFAULT_MIN_DISTANCE_METERS)
                .build()

            locationEngine.requestLocationUpdates(request, locationEngineCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            loge(e, "Error during location request")
        }
    }

    private fun stopLocationListener() {
        locationEngine.removeLocationUpdates(locationEngineCallback)
    }

    override fun getLocation(): Point? {
        if (!PermissionsManager.areLocationPermissionsGranted(app)) {
            return null
        }

        if (lastLocationInfo.timestamp + LOCATION_CACHE_TIME_MS <= timeProvider.currentTimeMillis()) {
            startLocationListener()
        }
        return lastLocationInfo.point
    }

    // Will be accessed from WrapperLocationProvider
    override fun getViewport(): LonLatBBox? {
        throw NotImplementedError()
    }

    private data class LocationInfo(
        val point: Point?,
        val timestamp: Long,
    )

    private companion object {
        private const val DEFAULT_MIN_TIME_MS = 0L
        private const val DEFAULT_MIN_DISTANCE_METERS = 0.0f
        private const val LOCATION_CACHE_TIME_MS = 30_000L
    }
}
