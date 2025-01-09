package com.mapbox.search.extensions.utils

import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import kotlinx.coroutines.suspendCancellableCoroutine

@JvmSynthetic
internal suspend fun LocationProvider.lastKnownLocation(): Point? {
    return suspendCancellableCoroutine { cont ->
        val cancelable = getLastLocation { location ->
            val result = location?.let {
                Point.fromLngLat(it.longitude, it.latitude)
            }
            cont.resumeWith(Result.success(result))
        }

        cont.invokeOnCancellation {
            cancelable.cancel()
        }
    }
}