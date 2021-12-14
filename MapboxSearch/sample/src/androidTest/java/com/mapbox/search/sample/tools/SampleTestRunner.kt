package com.mapbox.search.sample.tools

import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.util.Log
import androidx.test.runner.AndroidJUnitRunner

class SampleTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        try {
            return Instrumentation.newApplication(TestApp::class.java, context)
        } catch (e: Exception) {
            Log.e("SampleTestRunner", "Error during preparing new application", e)
            throw e
        }
    }
}
