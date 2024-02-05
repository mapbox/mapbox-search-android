package com.mapbox.search.ui.utils.extenstion

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import androidx.annotation.Px

internal fun View.showKeyboard() {
    post {
        context.inputMethodManager.showSoftInput(this, SHOW_IMPLICIT)
    }
}

/**
 * TODO
 *
 */
public fun View.hideKeyboard() {
    context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

internal val View.verticalPaddings: Int
    get() = paddingTop + paddingBottom

internal inline fun View.doOnWindowFocus(crossinline action: () -> Unit) {
    val activity = context.unwrapActivityOrNull() ?: return
    if (activity.hasWindowFocus()) {
        action()
    } else {
        activity.window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        action()
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

internal fun View.setMarginTop(@Px topMargin: Int) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = topMargin
}
