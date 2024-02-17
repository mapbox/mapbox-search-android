package com.mapbox.search.ui.utils.extenstion

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat

internal val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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
