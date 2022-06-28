package com.mapbox.search.ui.utils

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.resolveAttr

private class SearchSdkContextThemeWrapper(context: Context) : ContextThemeWrapper(
    context,
    context.resolveAttr(
        R.attr.mapboxSearchSdkTheme,
        R.style.MapboxSearchSdk_Theme_Auto
    )
)

internal fun wrapWithSearchTheme(context: Context): Context {
    return if (context is SearchSdkContextThemeWrapper) {
        context
    } else {
        SearchSdkContextThemeWrapper(context)
    }
}
