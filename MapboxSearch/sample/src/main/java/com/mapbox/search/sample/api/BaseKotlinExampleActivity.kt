package com.mapbox.search.sample.api

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.google.gson.GsonBuilder
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.R

@OptIn(MapboxExperimental::class)
abstract class BaseKotlinExampleActivity : AppCompatActivity() {

    private val logTextScroll: ScrollView by lazy { findViewById(R.id.logTextScroll) }
    private val toolbarView: Toolbar by lazy { findViewById(R.id.toolbar) }
    private val logTextView: TextView by lazy { findViewById(R.id.logText) }
    private val startButton: AppCompatButton by lazy { findViewById(R.id.startButton) }

    abstract val titleResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_code_example)

        toolbarView.setTitle(titleResId)
        toolbarView.setNavigationIcon(
            com.mapbox.search.ui.R.drawable.mapbox_search_sdk_close_drawable
        )
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

    // TODO we probably shouldn't try to print in JSON format by default,
    // because it prints all the object structure, including internal stuff
    protected fun logI(tag: String = LOG_TAG, message: String, result: Any? = null) {
        runOnUiThread {
            Log.i(tag, message)
            appendToLogMsg(message)
            result?.let { res ->
                Log.i(tag, message)
                appendToLogMsg(prettify(res))
            }
        }
    }

    protected fun logE(tag: String = LOG_TAG, message: String, error: Throwable? = null) {
        runOnUiThread {
            Log.e(tag, message, error)
            appendToLogMsg("$message: ${error?.message}")
        }
    }

    protected fun printMessage(message: String) {
        runOnUiThread {
            Log.i(LOG_TAG, message)
            appendToLogMsg(message)
        }
    }

    protected fun printMessage(results: List<OfflineSearchResult>) {
        printMessage("Results:\n${results.joinToString(separator = "\n") { it.toPrettyString() }}")
    }

    protected fun printSearchResults(results: List<SearchResult>) {
        printMessage("Results:\n${results.joinToString(separator = "\n") { it.toPrettyString() }}")
    }

    @SuppressLint("SetTextI18n")
    private fun appendToLogMsg(message: String) {
        logTextView.text = "${logTextView.text}\n$message"
        logTextScroll.post {
            logTextScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    protected companion object {

        const val LOG_TAG = "SearchApiExample"

        private val GSON = GsonBuilder().setPrettyPrinting().serializeNulls().create()

        fun prettify(result: Any) = GSON.toJson(result).toString()

        fun OfflineSearchResult.toPrettyString(): String {
            return "OfflineSearchResult(\n" +
                    "\tid='$id',\n" +
                    "\tmapboxId='$mapboxId',\n" +
                    "\tname='$name',\n" +
                    "\tdescriptionText=$descriptionText,\n" +
                    "\taddress=$address,\n" +
                    "\tcoordinate=$coordinate,\n" +
                    "\troutablePoints=$routablePoints,\n" +
                    "\tnewType=$newType,\n" +
                    "\tdistanceMeters=$distanceMeters,\n" +
                    "\tmetadata=$metadata\n" +
                    ")"
        }

        fun SearchResult.toPrettyString(): String {
            return "SearchResult(\n" +
                    "\tid='$id',\n" +
                    "\tmapboxId='$mapboxId',\n" +
                    "\tname='$name',\n" +
                    "\tmatchingName='$matchingName',\n" +
                    "\tdescriptionText=$descriptionText,\n" +
                    "\taddress=$address,\n" +
                    "\tfullAddress=$fullAddress,\n" +
                    "\tcoordinate=$coordinate,\n" +
                    "\troutablePoints=$routablePoints,\n" +
                    "\tboundingBox=$boundingBox,\n" +
                    "\tcategories=$categories,\n" +
                    "\tcategoryIds=$categoryIds,\n" +
                    "\tnewTypes=$newTypes,\n" +
                    "\tdistanceMeters=$distanceMeters,\n" +
                    "\tmetadata=$metadata\n" +
                    ")"
        }
    }
}
