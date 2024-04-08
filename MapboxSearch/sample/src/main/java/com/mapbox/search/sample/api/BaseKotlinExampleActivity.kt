package com.mapbox.search.sample.api

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.google.gson.GsonBuilder
import com.mapbox.search.sample.R

abstract class BaseKotlinExampleActivity : AppCompatActivity() {

    private val toolbarView: Toolbar by lazy { findViewById(R.id.toolbar) }
    private val logTextView: TextView by lazy { findViewById(R.id.logText) }
    private val startButton: AppCompatButton by lazy { findViewById(R.id.startButton) }

    abstract val titleResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_code_example)

        toolbarView.setTitle(titleResId)
        toolbarView.setNavigationIcon(R.drawable.mapbox_search_sdk_close_drawable)
        toolbarView.setNavigationOnClickListener {
            this.finish()
        }

        startButton.setOnClickListener {
            logTextView.text = getString(R.string.working)
            startButton.isEnabled = false
            startExample()
        }
    }

    abstract fun startExample()

    fun onFinished() {
        startButton.isEnabled = true
    }

    protected fun logI(tag: String, message: String, result: Any? = null) {
        Log.i(tag, message.addToLog())
        result?.let { res -> printObjectToLog(res) }
    }

    protected fun logE(tag: String, message: String, error: Throwable? = null) {
        Log.i(tag, "$message: ${error?.message}".addToLog(), error)
    }

    protected fun prettify(result: Any): String {
        val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
        return gson.toJson(result).toString()
    }

    private fun printObjectToLog(results: Any) {
        prettify(results).addToLog()
    }

    @SuppressLint("SetTextI18n")
    protected fun String.addToLog() = this.also {
        logTextView.text = "${logTextView.text}\n$this"
    }
}
