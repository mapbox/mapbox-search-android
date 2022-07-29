package com.mapbox.search.base.utils.extension

import android.content.pm.PackageInfo
import android.os.Build

val PackageInfo.versionCodeCompat: Long
    get() = if (Build.VERSION.SDK_INT >= 28) {
        longVersionCode
    } else {
        @Suppress("DEPRECATION")
        versionCode.toLong()
    }
