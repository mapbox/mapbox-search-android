package com.mapbox.search.common

import android.app.PendingIntent
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.internal.bindgen.LocationProvider
import com.mapbox.search.internal.bindgen.LonLatBBox

class FixedPointLocationEngine(
    private val location: Location,
    private val viewPort: BoundingBox? = null,
) : LocationEngine, LocationProvider {

    constructor(point: Point, viewPort: BoundingBox? = null) : this(point.toLocation(), viewPort)

    override fun getLocation(): Point? {
        return Point.fromLngLat(location.longitude, location.latitude)
    }

    override fun getViewport(): LonLatBBox? {
        return viewPort?.toLonLatBBox()
    }

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

        fun BoundingBox.toLonLatBBox(): LonLatBBox = LonLatBBox(southwest(), northeast())
    }
}
