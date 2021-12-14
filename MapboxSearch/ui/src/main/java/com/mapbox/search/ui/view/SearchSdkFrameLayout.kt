package com.mapbox.search.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Px
import androidx.core.content.withStyledAttributes
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.getPixelSize
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView

/**
 * Base class for bottom-sheet-like Search SDK views ([SearchBottomSheetView],
 * [SearchPlaceBottomSheetView], [SearchCategoriesBottomSheetView]).
 *
 * Note: Please, do not specify any background for this view. This will not have any affect, because this layout is a simple wrapper around actual bottom sheet view.
 */
public open class SearchSdkFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * Max width of the current view.
     */
    @Px
    public var maxWidth: Int = Int.MAX_VALUE
        set(value) {
            field = value
            requestLayout()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.SearchSdkFrameLayout, defStyleAttr, defStyleRes) {
            maxWidth = getDimensionPixelSize(
                R.styleable.SearchSdkFrameLayout_maxWidth,
                context.getPixelSize(R.dimen.mapbox_search_sdk_search_view_max_width)
            )
        }

        updateChildElevation(elevation)

        setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                updateChildElevation(elevation)
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {}
        })
    }

    /**
     * @suppress
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val newWidthMeasureSpec = if (maxWidth in (1 until measuredWidth)) {
            val measureMode = MeasureSpec.getMode(widthMeasureSpec)
            MeasureSpec.makeMeasureSpec(maxWidth, measureMode)
        } else {
            widthMeasureSpec
        }

        super.onMeasure(newWidthMeasureSpec, heightMeasureSpec)
    }

    /**
     * @suppress
     */
    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        updateChildElevation(elevation)
    }

    private fun updateChildElevation(@Px elevation: Float) {
        getChildAt(0)?.elevation = elevation
    }
}
