package com.mapbox.search.ui.utils

import android.content.Context
import androidx.annotation.StyleRes
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

private class SearchSdkPopupDialogContextThemeWrapper(
    context: Context,
    @StyleRes overlayTheme: Int
) : ContextThemeWrapper(context, overlayTheme)

internal fun wrapWithSearchTheme(context: Context): Context {
    return if (context is SearchSdkContextThemeWrapper) {
        context
    } else {
        SearchSdkContextThemeWrapper(context)
    }
}

internal fun wrapWithSearchPopupDialogThemeOverlay(context: Context): Context {
    if (context !is SearchSdkPopupDialogContextThemeWrapper) {
        val overlayTheme = context.resolveAttr(R.attr.mapboxSearchSdkPopupDialogThemeOverlay)
        if (overlayTheme != null) {
            return SearchSdkPopupDialogContextThemeWrapper(context, overlayTheme)
        }
    }
    return context
}
