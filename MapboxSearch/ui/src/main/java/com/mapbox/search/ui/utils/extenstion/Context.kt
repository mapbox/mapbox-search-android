package com.mapbox.search.ui.utils.extenstion

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.view.place.SearchPlace

internal val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

internal val Context.locationManager: LocationManager
    get() = getSystemService(Context.LOCATION_SERVICE) as LocationManager

@ColorInt
internal fun Context.getColorCompat(@ColorRes colorId: Int): Int {
    return ContextCompat.getColor(this, colorId)
}

internal fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    return when (id) {
        0 -> null
        else -> ContextCompat.getDrawable(this, id)
    }
}

internal fun Context.unwrapActivityOrNull(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.unwrapActivityOrNull()
        else -> null
    }
}

@Px
internal fun Context.getPixelSize(@DimenRes dimension: Int): Int {
    return resources.getDimensionPixelSize(dimension)
}

internal fun Context.resolveAttrOrThrow(@AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    return if (theme.resolveAttribute(resId, typedValue, true)) {
        typedValue.data
    } else {
        error("Couldn't resolve attribute \"${resources.getResourceEntryName(resId)}\"!")
    }
}

internal fun Context.resolveAttr(@AttrRes resId: Int, defaultValue: Int): Int {
    val typedValue = TypedValue()
    return if (theme.resolveAttribute(resId, typedValue, true)) {
        typedValue.data
    } else {
        defaultValue
    }
}

internal fun Context.resolveAttr(@AttrRes resId: Int): Int? {
    val typedValue = TypedValue()
    return if (theme.resolveAttribute(resId, typedValue, true)) {
        typedValue.data
    } else {
        null
    }
}

/**
 * TODO
 *
 */
public fun Context.showToast(@StringRes resId: Int): Unit = Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

/**
 * TODO
 *
 */
public fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * TODO
 *
 */
public fun Context.bitmapFromDrawableRes(@DrawableRes resId: Int): Bitmap = BitmapFactory.decodeResource(resources, resId)

/**
 * TODO
 *
 */
public fun dpToPx(dp: Int): Int = (dp * Resources.getSystem().displayMetrics.density).toInt()

/**
 * TODO
 *
 */
public fun geoIntent(point: Point): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}"))

/**
 * TODO
 *
 */
public fun shareIntent(searchPlace: SearchPlace): Intent {
    val text = "${searchPlace.name}. " +
            "Address: ${searchPlace.address?.formattedAddress(SearchAddress.FormatStyle.Short) ?: "unknown"}. " +
            "Geo coordinate: (lat=${searchPlace.coordinate.latitude()}, lon=${searchPlace.coordinate.longitude()})"

    return Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
}
