package com.mapbox.search.ui.view.category

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setTintCompat

internal class HotCategoryView : LinearLayout {

    var category: Category? = null
        set(value) {
            if (field != value) {
                field = value
                populate(field)
            }
        }

    private val icon: ImageView
    private val title: TextView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        View.inflate(context, R.layout.mapbox_search_sdk_hot_category_layout, this)

        icon = findViewById(R.id.icon)
        title = findViewById(R.id.title)
    }

    private fun populate(category: Category?) {
        if (category == null) {
            icon.setImageDrawable(null)
            title.text = null
        } else {
            category.presentation.also {
                val drawable = context.getDrawableCompat(it.icon)
                    ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkIconTintColor))
                icon.setImageDrawable(drawable)
                title.setText(it.displayName)
            }
        }
    }
}
