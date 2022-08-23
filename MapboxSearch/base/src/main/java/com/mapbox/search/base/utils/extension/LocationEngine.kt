package com.mapbox.search.base.utils.extension

import android.annotation.SuppressLint
import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask

@SuppressLint("MissingPermission")
fun LocationEngine.lastKnownLocationOrNull(context: Context, callback: (Point?) -> Unit): AsyncOperationTask {
    if (!PermissionsManager.areLocationPermissionsGranted(context)) {
        callback(null)
        return AsyncOperationTaskImpl.COMPLETED
    }

    val task = AsyncOperationTaskImpl<Any>()
    val locationCallback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            val location = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                Point.fromLngLat(location.longitude, location.latitude)
            }
            if (!task.isCancelled) {
                callback(location)
                task.onComplete()
            }
        }

        override fun onFailure(e: Exception) {
            if (!task.isCancelled) {
                callback(null)
                task.onComplete()
            }
        }
    }
    task.onCancelCallback = {
        removeLocationUpdates(locationCallback)
    }
    getLastLocation(locationCallback)
    return task
}
