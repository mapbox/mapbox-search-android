package com.mapbox.search.common.tests

import android.app.PendingIntent
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.Cancelable
import com.mapbox.common.location.AccuracyAuthorization
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.DeviceLocationProviderFactory
import com.mapbox.common.location.DeviceLocationProviderType
import com.mapbox.common.location.GetLocationCallback
import com.mapbox.common.location.LocationError
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceObserver
import com.mapbox.common.location.PermissionStatus
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.internal.bindgen.LocationProvider
import com.mapbox.search.internal.bindgen.LonLatBBox

class FixedPointLocationEngine(
    private val location: Location,
    private val viewPort: BoundingBox? = null,
) : LocationService, LocationProvider {

    constructor(point: Point, viewPort: BoundingBox? = null) : this(point.toLocation(), viewPort)

    override fun getLocation(): Point? {
        return Point.fromLngLat(location.longitude, location.latitude)
    }

    override fun getViewport(): LonLatBBox? {
        return viewPort?.toLonLatBBox()
    }

    override fun getAccuracyAuthorization() = AccuracyAuthorization.EXACT

    override fun getDeviceLocationProvider(
        type: DeviceLocationProviderType,
        request: LocationProviderRequest?
    ): Expected<LocationError, DeviceLocationProvider> {
        return getDeviceLocationProvider(request)
    }

    override fun getDeviceLocationProvider(
        request: LocationProviderRequest?
    ): Expected<LocationError, DeviceLocationProvider> {
        return ExpectedFactory.createValue(FixedDeviceLocationProvider(location))
    }

    override fun getPermissionStatus() = PermissionStatus.FOREGROUND

    override fun isAvailable() = true

    override fun registerObserver(observer: LocationServiceObserver) {
        // do nothing
    }

    override fun setUserDefinedDeviceLocationProviderFactory(factory: DeviceLocationProviderFactory?) {
        // do nothing
    }

    override fun unregisterObserver(observer: LocationServiceObserver) {
        // do nothing
    }

    private class FixedDeviceLocationProvider(
        location: Location
    ) : DeviceLocationProvider {

        private val mapboxLocation = com.mapbox.common.location.Location.Builder()
            .longitude(location.longitude)
            .latitude(location.latitude)
            .build()

        override fun addLocationObserver(observer: LocationObserver, looper: Looper) {
            Handler(looper).post {
                observer.onLocationUpdateReceived(listOf(mapboxLocation))
            }
        }

        override fun addLocationObserver(observer: LocationObserver) {
            observer.onLocationUpdateReceived(listOf(mapboxLocation))
        }

        override fun getLastLocation(callback: GetLocationCallback): Cancelable {
            callback.run(mapboxLocation)
            return STUB_CANCELABLE
        }

        override fun removeLocationObserver(observer: LocationObserver) {
            // do nothing
        }

        override fun removeLocationUpdates(pendingIntent: PendingIntent) {
            // do nothing
        }

        override fun requestLocationUpdates(pendingIntent: PendingIntent) {
            // do nothing
        }
    }

    private companion object {

        val STUB_CANCELABLE = Cancelable {
            // do nothing
        }

        fun Point.toLocation(): Location {
            val location = Location("")
            location.latitude = latitude()
            location.longitude = longitude()
            return location
        }

        fun BoundingBox.toLonLatBBox(): LonLatBBox = LonLatBBox(southwest(), northeast())
    }
}
