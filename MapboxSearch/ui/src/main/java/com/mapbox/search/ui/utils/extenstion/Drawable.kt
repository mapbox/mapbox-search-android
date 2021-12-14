package com.mapbox.search.ui.utils.extenstion

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

internal fun Drawable.setTintCompat(@ColorInt color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN): Drawable {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
        DrawableCompat.wrap(this).mutate().apply {
            DrawableCompat.setTintMode(this, mode)
            DrawableCompat.setTint(this, color)
        }
    } else {
        mutate().apply {
            colorFilter = PorterDuffColorFilter(color, mode)
        }
    }
}
