package com.mapbox.search.common

import android.app.PendingIntent
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point

class FixedPointLocationEngine(val location: Location) : LocationEngine {

    constructor(point: Point) : this(point.toLocation())

    override fun getLastLocation(locationEngineCallback: LocationEngineCallback<LocationEngineResult>) {
        locationEngineCallback.onSuccess(LocationEngineResult.create(location))
    }

    override fun requestLocationUpdates(
        locationEngineRequest: LocationEngineRequest,
        locationEngineCallback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper?
    ) {
        val callbackRunnable = Runnable {
            locationEngineCallback.onSuccess(LocationEngineResult.create(location))
        }

        if (looper != null) {
            Handler(looper).post(callbackRunnable)
        } else {
            callbackRunnable.run()
        }
    }

    override fun requestLocationUpdates(locationEngineRequest: LocationEngineRequest, pendingIntent: PendingIntent?) {
        throw NotImplementedError()
    }

    override fun removeLocationUpdates(locationEngineCallback: LocationEngineCallback<LocationEngineResult>) {
        // Do nothing
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
        // Do nothing
    }

    private companion object {
        fun Point.toLocation(): Location {
            val location = Location("")
            location.latitude = latitude()
            location.longitude = longitude()
            return location
        }
    }
}
