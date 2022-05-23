package com.mapbox.search.utils

import android.content.Context
import android.os.Build

internal interface AppInfoProvider {
    val searchSdkPackageName: String
    val searchSdkVersionName: String
    val deviceModel: String
    val deviceName: String
    val osVersion: String
    val appPackageName: String
    val appVersion: String
}

internal class AppInfoProviderImpl(
    private val context: Context,
    override val searchSdkPackageName: String,
    override val searchSdkVersionName: String,
) : AppInfoProvider {

    override val deviceModel: String
        get() = Build.MODEL

    override val deviceName: String
        get() = Build.DEVICE

    override val osVersion: String
        get() = Build.VERSION.RELEASE

    override val appPackageName: String
        get() = context.packageName

    override val appVersion: String by lazy {
        try {
            val packageName = context.packageName
            context.packageManager.getPackageInfo(packageName, 0).versionName
        } catch (exception: Exception) {
            "unknown"
        }
    }
}
