package com.mapbox.search.ui.view.common

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ProgressBar
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.resolveAttr

internal class MapboxSdkProgressBar : ProgressBar {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        val progressColor = context.resolveAttr(R.attr.mapboxSearchSdkProgressColor)
            ?: context.resolveAttr(R.attr.mapboxSearchSdkPrimaryAccentColor)
            ?: error(
                "Couldn't resolve attribute one of the following attributes: " +
                    "\"mapboxSearchSdkProgressColor\", \"mapboxSearchSdkPrimaryAccentColor\"!"
            )
        val progressColorStateList = ColorStateList.valueOf(progressColor)
        progressTintList = progressColorStateList
        indeterminateTintList = progressColorStateList
    }
}
