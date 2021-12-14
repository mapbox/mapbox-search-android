package com.mapbox.search.sample

import android.content.Context
import androidx.multidex.MultiDex

class DebugSampleApplication : SampleApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}
