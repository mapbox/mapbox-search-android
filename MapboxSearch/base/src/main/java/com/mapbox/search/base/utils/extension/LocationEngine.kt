package com.mapbox.search.base.utils.extension

import android.annotation.SuppressLint
import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.Point
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import kotlinx.coroutines.suspendCancellableCoroutine

@SuppressLint("MissingPermission")
fun LocationEngine.lastKnownLocation(context: Context, callback: (Expected<Exception, Point>) -> Unit): AsyncOperationTask {
    if (!PermissionsManager.areLocationPermissionsGranted(context)) {
        callback(ExpectedFactory.createError(Exception("Location permissions are not granted")))
        return AsyncOperationTaskImpl.COMPLETED
    }

    val task = AsyncOperationTaskImpl<Any>()
    val locationCallback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            val location: Point? = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                Point.fromLngLat(location.longitude, location.latitude)
            }
            if (!task.isCancelled) {
                val res: Expected<Exception, Point> = if (location == null) {
                    ExpectedFactory.createError(Exception("Unknown location"))
                } else {
                    ExpectedFactory.createValue(location)
                }
                callback(res)
                task.onComplete()
            }
        }

        override fun onFailure(exception: Exception) {
            if (!task.isCancelled) {
                callback(ExpectedFactory.createError(exception))
                task.onComplete()
            }
        }
    }
    task.addOnCancelledCallback {
        removeLocationUpdates(locationCallback)
    }
    getLastLocation(locationCallback)
    return task
}

fun LocationEngine.lastKnownLocationOrNull(context: Context, callback: (Point?) -> Unit): AsyncOperationTask {
    return lastKnownLocation(context) { result ->
        result.onValue(callback).onError {
            callback(null)
        }
    }
}

suspend fun LocationEngine.lastKnownLocation(context: Context): Expected<Exception, Point> {
    return suspendCancellableCoroutine { continuation ->
        val task = lastKnownLocation(context) {
            continuation.resumeWith(Result.success(it))
        }

        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
}
