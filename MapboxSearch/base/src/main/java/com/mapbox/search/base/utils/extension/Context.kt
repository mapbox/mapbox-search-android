package com.mapbox.search.base.utils.extension

import android.content.Context
import android.content.res.Configuration

val Context.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
