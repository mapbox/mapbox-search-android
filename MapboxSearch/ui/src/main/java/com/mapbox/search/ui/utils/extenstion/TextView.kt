package com.mapbox.search.ui.utils.extenstion

import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible

internal fun TextView.setTextAndHideIfBlank(text: String?) {
    this.text = text
    isVisible = !text.isNullOrBlank()
}

internal fun TextView.setCompoundDrawableStartWithIntrinsicBounds(@DrawableRes start: Int) {
    if (isLayoutDirectionLtr) {
        setCompoundDrawableLeftWithIntrinsicBounds(left = start)
    } else {
        setCompoundDrawableRightWithIntrinsicBounds(right = start)
    }
}

internal fun TextView.setCompoundDrawableEndWithIntrinsicBounds(@DrawableRes end: Int) {
    if (isLayoutDirectionLtr) {
        setCompoundDrawableRightWithIntrinsicBounds(right = end)
    } else {
        setCompoundDrawableLeftWithIntrinsicBounds(left = end)
    }
}

internal fun TextView.setCompoundDrawableLeftWithIntrinsicBounds(@DrawableRes left: Int) {
    setCompoundDrawableLeftWithIntrinsicBounds(context.getDrawableCompat(left))
}

internal fun TextView.setCompoundDrawableTopWithIntrinsicBounds(@DrawableRes top: Int) {
    setCompoundDrawableTopWithIntrinsicBounds(context.getDrawableCompat(top))
}

internal fun TextView.setCompoundDrawableRightWithIntrinsicBounds(@DrawableRes right: Int) {
    setCompoundDrawableRightWithIntrinsicBounds(context.getDrawableCompat(right))
}

internal fun TextView.setCompoundDrawableBottomWithIntrinsicBounds(@DrawableRes bottom: Int) {
    setCompoundDrawableBottomWithIntrinsicBounds(context.getDrawableCompat(bottom))
}

internal fun TextView.setCompoundDrawableStartWithIntrinsicBounds(start: Drawable?) {
    if (isLayoutDirectionLtr) {
        setCompoundDrawableLeftWithIntrinsicBounds(left = start)
    } else {
        setCompoundDrawableRightWithIntrinsicBounds(right = start)
    }
}

internal fun TextView.setCompoundDrawableEndWithIntrinsicBounds(end: Drawable?) {
    if (isLayoutDirectionLtr) {
        setCompoundDrawableRightWithIntrinsicBounds(right = end)
    } else {
        setCompoundDrawableLeftWithIntrinsicBounds(left = end)
    }
}

internal fun TextView.setCompoundDrawableLeftWithIntrinsicBounds(left: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(left, drawableTop, drawableRight, drawableBottom)
}

internal fun TextView.setCompoundDrawableTopWithIntrinsicBounds(top: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(drawableLeft, top, drawableRight, drawableBottom)
}

internal fun TextView.setCompoundDrawableRightWithIntrinsicBounds(right: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, right, drawableBottom)
}

internal fun TextView.setCompoundDrawableBottomWithIntrinsicBounds(bottom: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, bottom)
}

internal var TextView.drawableStart: Drawable?
    get() = if (isLayoutDirectionLtr) {
        drawableLeft
    } else {
        drawableRight
    }
    set(value) {
        if (isLayoutDirectionLtr) {
            drawableLeft = value
        } else {
            drawableRight = value
        }
    }

internal var TextView.drawableEnd: Drawable?
    get() = if (isLayoutDirectionLtr) {
        drawableRight
    } else {
        drawableLeft
    }
    set(value) {
        if (isLayoutDirectionLtr) {
            drawableRight = value
        } else {
            drawableLeft = value
        }
    }

internal var TextView.drawableLeft: Drawable?
    get() = compoundDrawables[0]
    set(value) {
        setCompoundDrawables(value, drawableTop, drawableRight, drawableBottom)
    }

internal var TextView.drawableTop: Drawable?
    get() = compoundDrawables[1]
    set(value) {
        setCompoundDrawables(drawableLeft, value, drawableRight, drawableBottom)
    }

internal var TextView.drawableRight: Drawable?
    get() = compoundDrawables[2]
    set(value) {
        setCompoundDrawables(drawableLeft, drawableTop, value, drawableBottom)
    }

internal var TextView.drawableBottom: Drawable?
    get() = compoundDrawables[3]
    set(value) {
        setCompoundDrawables(drawableLeft, drawableTop, drawableRight, value)
    }
