package com.mapbox.search.common.extension

import android.content.Context
import android.content.res.Configuration

val Context.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
