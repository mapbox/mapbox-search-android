package com.mapbox.search.ui.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.withStyledAttributes
import com.mapbox.search.ui.R

internal class MapboxFadeView : View {

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }
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
        context.withStyledAttributes(attrs, R.styleable.MapboxFadeView, defStyleAttr, defStyleRes) {
            val mode: Mode = when (val modeValue = getInt(R.styleable.MapboxFadeView_mode, 0)) {
                0 -> Mode.NORMAL
                1 -> Mode.LIGHT
                else -> error("Unknown mode int value: $modeValue")
            }
            if (!hasValue(R.styleable.MapboxFadeView_fadeColor)) {
                error("Attribute \"fadeColor\" must be set for MapboxFadeView!")
            }
            val fadeColor = getColorOrThrow(R.styleable.MapboxFadeView_fadeColor)

            val backgroundDrawable = when (mode) {
                Mode.NORMAL -> {
                    GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(fadeColor, fadeColor, 0x00FFFFFF and fadeColor)
                    )
                }
                Mode.LIGHT -> {
                    GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(fadeColor, 0x00FFFFFF and fadeColor)
                    )
                }
            }
            background = backgroundDrawable
        }
    }

    internal enum class Mode {
        NORMAL,
        LIGHT;
    }
}
