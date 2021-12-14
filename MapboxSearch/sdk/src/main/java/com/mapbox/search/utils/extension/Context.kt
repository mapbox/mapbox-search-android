package com.mapbox.search.utils.extension

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

internal val Context.packageInfoOrNull: PackageInfo?
    get() = try {
        packageManager.getPackageInfo(packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
