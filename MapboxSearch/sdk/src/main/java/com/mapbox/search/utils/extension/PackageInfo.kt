package com.mapbox.search.utils.extension

import android.content.pm.PackageInfo
import android.os.Build

internal val PackageInfo.versionCodeCompat: Long
    get() = if (Build.VERSION.SDK_INT >= 28) {
        longVersionCode
    } else {
        @Suppress("DEPRECATION")
        versionCode.toLong()
    }
