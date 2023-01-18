package com.mapbox.search.sample

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.toCameraOptions
import com.mapbox.search.ServiceProvider

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

fun LocationEngine.userDistanceTo(context: Context, destination: Point, callback: (Double?) -> Unit) {
    lastKnownLocation(context) { location ->
        if (location == null) {
            callback(null)
        } else {
            val distance = ServiceProvider.INSTANCE
                .distanceCalculator(latitude = location.latitude())
                .distance(location, destination)
            callback(distance)
        }
    }
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

fun Context.bitmapFromDrawableRes(@DrawableRes resourceId: Int): Bitmap? {
    return AppCompatResources.getDrawable(this, resourceId)?.toBitmap()
}

fun Drawable.toBitmap(): Bitmap? {
    return if (this is BitmapDrawable) {
        bitmap
    } else {
        // copying drawable object to not manipulate on the same reference
        val constantState = constantState ?: return null
        val drawable = constantState.newDrawable().mutate()
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun MapboxMap.getCameraBoundingBox(): BoundingBox {
    val bounds = coordinateBoundsForCamera(cameraState.toCameraOptions())
    return BoundingBox.fromPoints(bounds.southwest, bounds.northeast)
}
