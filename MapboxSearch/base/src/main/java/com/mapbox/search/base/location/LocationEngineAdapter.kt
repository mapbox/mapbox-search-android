package com.mapbox.search.base.location

import android.annotation.SuppressLint
import android.app.Application
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.common.Cancelable
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreLocationProvider
import com.mapbox.search.base.logger.logi
import com.mapbox.search.base.utils.LocalTimeProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.internal.bindgen.LonLatBBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Maximum time period for waiting for location updates (in milliseconds)
private var locationEngineObservationTimeout: Long? = 200L

@Suppress("UNUSED")
fun setLocationEngineLocationObservationTimeout(timeout: Long?) {
    locationEngineObservationTimeout = timeout
}

// Suppressed because we check permission but lint can't detekt it
@SuppressLint("MissingPermission")
class LocationEngineAdapter(
    private val app: Application,
    private val locationProvider: LocationProvider?,
    private val timeProvider: TimeProvider = LocalTimeProvider(),
    private val locationPermissionChecker: (Application) -> Boolean = {
        PermissionsManager.areLocationPermissionsGranted(app)
    },
) : CoreLocationProvider {

    @Volatile
    private var lastLocationInfo = LocationInfo(null, 0)

    private var timeoutWatcherJob: Job? = null

    private val locationObserver = LocationObserver { locations ->
        locations.firstOrNull()?.let {
            lastLocationInfo = LocationInfo(it.toPoint(), timeProvider.currentTimeMillis())
        }
        stopLocationListener()
    }

    private var locationCancelable: Cancelable? = null

    init {
        if (!locationPermissionChecker(app)) {
            logi("Location permission is not granted")
        } else {
            locationCancelable = locationProvider?.getLastLocation { location: Location? ->
                if (location == null) {
                    startLocationListener()
                } else {
                    lastLocationInfo = LocationInfo(location.toPoint(), timeProvider.currentTimeMillis())
                }
            }
        }
    }

    private fun startLocationListener() {
        locationProvider?.addLocationObserver(locationObserver)

        locationEngineObservationTimeout?.let { timeout ->
            timeoutWatcherJob?.cancel()
            timeoutWatcherJob = CoroutineScope(Job()).launch {
                delay(timeout)
                timeoutWatcherJob = null
                stopLocationListener()
            }
        }
    }

    private fun stopLocationListener() {
        locationProvider?.removeLocationObserver(locationObserver)
        locationCancelable?.cancel()
        timeoutWatcherJob?.cancel()
    }

    override fun getLocation(): Point? {
        if (!locationPermissionChecker(app)) {
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
        private const val LOCATION_CACHE_TIME_MS = 30_000L
    }
}
