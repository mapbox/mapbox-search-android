package com.mapbox.search.ui.view.common

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.withStyledAttributes
import androidx.transition.TransitionManager
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat
import com.mapbox.search.ui.utils.extenstion.setCompoundDrawableStartWithIntrinsicBounds
import com.mapbox.search.ui.utils.extenstion.setTintCompat

internal class MapboxSdkButtonWithIcon : FrameLayout {

    private var iconTintColor: Int? = null
    private lateinit var textView: TextView

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        textView = TextView(context, attrs)
        textView.id = R.id.mapbox_sdk_button_text_view
        textView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }

        addView(textView)

        context.withStyledAttributes(attrs, R.styleable.MapboxSdkButtonWithIcon, defStyleAttr, defStyleRes) {
            if (hasValue(R.styleable.MapboxSdkButtonWithIcon_drawableTintCompat)) {
                iconTintColor = getColor(R.styleable.MapboxSdkButtonWithIcon_drawableTintCompat, -1)

                getDrawable(R.styleable.MapboxSdkButtonWithIcon_iconSrc)?.apply {
                    textView.setCompoundDrawableStartWithIntrinsicBounds(setTintCompat(iconTintColor!!))
                }
            }
        }
    }

    fun setText(@StringRes resId: Int, animate: Boolean = true) {
        if (animate) {
            TransitionManager.beginDelayedTransition(this)
        }
        textView.setText(resId)
    }

    fun setIcon(@DrawableRes resId: Int, animate: Boolean = true) {
        if (animate) {
            TransitionManager.beginDelayedTransition(this)
        }

        val drawable = context.getDrawableCompat(resId)?.run {
            val tint = iconTintColor
            if (tint != null) {
                setTintCompat(tint)
            } else {
                this
            }
        }
        textView.setCompoundDrawableStartWithIntrinsicBounds(drawable)
    }
}
