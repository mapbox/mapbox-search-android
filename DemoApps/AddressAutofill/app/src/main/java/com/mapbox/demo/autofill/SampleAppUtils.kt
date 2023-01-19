package com.mapbox.demo.autofill

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point

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

fun View.hideKeyboard() {
    context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.showToast(@StringRes resId: Int) {
    Toast.makeText(applicationContext, getString(resId), Toast.LENGTH_SHORT).show()
}

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
