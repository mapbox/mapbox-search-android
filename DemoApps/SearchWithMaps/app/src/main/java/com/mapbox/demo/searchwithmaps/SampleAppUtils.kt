package com.mapbox.demo.searchwithmaps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.view.place.SearchPlace

@SuppressLint("MissingPermission")
fun LocationEngine.lastKnownLocation(context: Context, callback: (Point?) -> Unit) {
    if (!PermissionsManager.areLocationPermissionsGranted(context)) {
        callback(null)
    }

    getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult?) {
            val location = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                Point.fromLngLat(location.longitude, location.latitude)
            }
            callback(location)
        }

        override fun onFailure(exception: Exception) {
            callback(null)
        }
    })
}

/**
 * TODO
 *
 */
fun LocationEngine.userDistanceTo(context: Context, destination: Point, callback: (Double?) -> Unit) {
    lastKnownLocation(context) { location ->
        if (location == null) {
            callback(null)
        } else {
            val distance = DistanceCalculator.instance(latitude = location.latitude())
                .distance(location, destination)
            callback(distance)
        }
    }
}

/**
 * TODO
 *
 */
fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun geoIntent(point: Point): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}"))
}

fun shareIntent(searchPlace: SearchPlace): Intent {
    val text = "${searchPlace.name}. " +
            "Address: ${searchPlace.address?.formattedAddress(SearchAddress.FormatStyle.Short) ?: "unknown"}. " +
            "Geo coordinate: (lat=${searchPlace.coordinate.latitude()}, lon=${searchPlace.coordinate.longitude()})"

    return Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
}
