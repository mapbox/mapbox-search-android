package com.mapbox.search.sample

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.mapbox.common.location.LocationService
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.toCameraOptions
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.view.place.SearchPlace

fun LocationService.lastKnownLocation(
    callback: (Point?) -> Unit
) {
    getDeviceLocationProvider(null).onValue {
        it.getLastLocation { location ->
            val res = if (location == null) {
                null
            } else {
                Point.fromLngLat(location.longitude, location.latitude)
            }
            callback(res)
        }
    }.onError {
        callback(null)
    }
}

fun LocationService.userDistanceTo(destination: Point, callback: (Double?) -> Unit) {
    lastKnownLocation { location ->
        if (location == null) {
            callback(null)
        } else {
            val distance = DistanceCalculator.instance(latitude = location.latitude())
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
    showToast(getString(resId))
}

fun Context.showToast(text: CharSequence) {
    Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
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
