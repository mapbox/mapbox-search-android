package com.mapbox.search.common.tests

import android.os.Handler
import android.os.Looper
import com.mapbox.common.Cancelable
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.GetLocationCallback
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point

class FixedPointLocationEngine(
    private val location: Location,
) : LocationProvider {

    constructor(point: Point, @Suppress("UNUSED_PARAMETER") viewPort: BoundingBox? = null) : this(point.toLocation())

    override fun addLocationObserver(observer: LocationObserver) {
        val callbackRunnable = Runnable {
            observer.onLocationUpdateReceived(listOf(location))
        }

        callbackRunnable.run()
    }

    override fun addLocationObserver(observer: LocationObserver, looper: Looper) {
        val callbackRunnable = Runnable {
            observer.onLocationUpdateReceived(listOf(location))
        }

        Handler(looper).post(callbackRunnable)
    }

    override fun getLastLocation(callback: GetLocationCallback): Cancelable {
        callback.run(location)
        return Cancelable { }
    }

    override fun removeLocationObserver(observer: LocationObserver) {
        // Do nothing
    }

    private companion object {

        fun Point.toLocation(): Location {
            return Location.Builder()
                .latitude(latitude())
                .longitude(longitude())
                .build()
        }

    }
}
