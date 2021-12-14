package com.mapbox.search.ui.view.category

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.verticalPaddings
import kotlin.math.max

internal class HotCategoriesView : ViewGroup {

    private var iconWidth: Int = 0

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
        context.withStyledAttributes(attrs, R.styleable.HotCategoriesView, defStyleAttr, defStyleRes) {
            iconWidth = getDimensionPixelSize(R.styleable.HotCategoriesView_categoryItemIconWidth, 0)
            check(iconWidth > 0) {
                "Icon width must be > 0"
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // HotCategoriesView width = childCount * iconWidth + (childCount + 1) * gap
        val parentWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val gap = (parentWidthSize - childCount * iconWidth) / (childCount + 1)

        val calculatedItemWidth = iconWidth + gap
        val edgePadding = gap / 2
        setPadding(edgePadding, paddingTop, edgePadding, paddingBottom)

        val heightConstraints = verticalPaddings
        var maxHeight = 0

        children.forEach {
            measureItemView(
                child = it,
                parentHeightMeasureSpec = heightMeasureSpec,
                calculatedItemWidth = calculatedItemWidth,
                heightUsed = heightConstraints
            )

            maxHeight = max(maxHeight, it.measuredHeight)
        }

        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            MeasureSpec.makeMeasureSpec(maxHeight + verticalPaddings, MeasureSpec.EXACTLY)
        )
    }

    private fun measureItemView(
        child: View,
        parentHeightMeasureSpec: Int,
        calculatedItemWidth: Int,
        heightUsed: Int
    ) {
        val lp = child.layoutParams as LayoutParams

        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(calculatedItemWidth, MeasureSpec.EXACTLY)

        val heightMeasureSpec = getChildMeasureSpec(
            parentHeightMeasureSpec,
            heightUsed,
            lp.height
        )

        child.measure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var nextChildLeft = paddingLeft
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val childTop = paddingTop
            child.layout(
                nextChildLeft,
                childTop,
                nextChildLeft + child.measuredWidth,
                child.measuredHeight + childTop
            )

            nextChildLeft += child.measuredWidth
        }
    }
}
