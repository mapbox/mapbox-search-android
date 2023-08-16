package com.mapbox.search.base.utils.extension

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationService
import com.mapbox.geojson.Point
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import kotlinx.coroutines.suspendCancellableCoroutine

fun LocationService.lastKnownLocationCommon(
    callback: (Expected<Exception, Location>) -> Unit
): AsyncOperationTask {
    val task = AsyncOperationTaskImpl(callback)
    getDeviceLocationProvider(null).onValue {
        task += it.getLastLocation { location ->
            task.markExecutedAndRunOnCallback {
                val res: Expected<Exception, Location> = if (location == null) {
                    ExpectedFactory.createError(Exception("Unknown location"))
                } else {
                    ExpectedFactory.createValue(location)
                }
                invoke(res)
            }
        }
    }.onError {
        task.markExecutedAndRunOnCallback {
            invoke(ExpectedFactory.createError(Exception(it.toString())))
        }
    }
    return task
}

fun LocationService.lastKnownLocation(
    callback: (Expected<Exception, Point>) -> Unit
): AsyncOperationTask {
    return lastKnownLocationCommon { result ->
        callback(result.mapValue { Point.fromLngLat(it.longitude, it.latitude) })
    }
}

fun LocationService.lastKnownLocationOrNull(callback: (Point?) -> Unit): AsyncOperationTask {
    return lastKnownLocation { result ->
        result.onValue(callback).onError {
            callback(null)
        }
    }
}

suspend fun LocationService.lastKnownLocation(): Expected<Exception, Point> {
    return suspendCancellableCoroutine { continuation ->
        val task = lastKnownLocation {
            continuation.resumeWith(Result.success(it))
        }

        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
}
