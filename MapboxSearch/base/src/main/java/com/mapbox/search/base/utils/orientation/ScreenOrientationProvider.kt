package com.mapbox.search.base.utils.orientation

import android.app.Application
import android.content.res.Configuration

fun interface ScreenOrientationProvider {
    fun provideOrientation(): ScreenOrientation?
}

class AndroidScreenOrientationProvider(
    private val application: Application
) : ScreenOrientationProvider {

    override fun provideOrientation(): ScreenOrientation {
        return when (val orientation = application.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.LANDSCAPE
            Configuration.ORIENTATION_PORTRAIT -> ScreenOrientation.PORTRAIT
            else -> throw IllegalStateException("Unknown android orientation code (= $orientation)")
        }
    }
}
