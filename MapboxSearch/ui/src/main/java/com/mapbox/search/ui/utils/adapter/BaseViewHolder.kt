package com.mapbox.search.ui.utils.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

internal abstract class BaseViewHolder<T>(parent: ViewGroup, @LayoutRes layoutRes: Int) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
) {

    protected val context: Context
        get() = itemView.context

    protected val resources: Resources
        get() = context.resources

    protected fun <T : View> findViewById(@IdRes idRes: Int): T {
        return itemView.findViewById(idRes)
    }

    abstract fun bind(item: T)
}
