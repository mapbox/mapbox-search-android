package com.mapbox.search.ui.utils

import android.content.Context
import android.graphics.Canvas
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat

internal class OffsetItemDecoration(
    context: Context,
    @DimenRes offsetDimenRes: Int = R.dimen.mapbox_search_sdk_primary_layout_offset,
    @DrawableRes drawableRes: Int = R.drawable.mapbox_search_sdk_list_divider,
    private val excludeLastItem: Boolean = false,
    private val applyPredicate: (RecyclerView.ViewHolder) -> Boolean = { true }
) : RecyclerView.ItemDecoration() {

    private val drawable = context.getDrawableCompat(drawableRes)
    private val offsetPx = context.resources.getDimension(offsetDimenRes).toInt()

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawable ?: return
        val childCount = parent.adapter?.itemCount ?: return

        val left = parent.paddingLeft + offsetPx
        val right = parent.width - parent.paddingRight - offsetPx

        val bound = if (excludeLastItem) {
            childCount - 1
        } else {
            childCount
        }

        for (i in 0 until bound) {
            val child = parent.getChildAt(i) ?: continue
            val viewHolder = parent.getChildViewHolder(child)
            if (!applyPredicate(viewHolder)) {
                continue
            }

            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + drawable.intrinsicHeight
            drawable.setBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }
    }
}
