package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.search.ServiceProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.HistoryRecord

class HistoryDataProviderKotlinExample : AppCompatActivity() {

    private val historyDataProvider = ServiceProvider.INSTANCE.historyDataProvider()

    private lateinit var task: AsyncOperationTask

    private val callback: CompletionCallback<List<HistoryRecord>> = object : CompletionCallback<List<HistoryRecord>> {
        override fun onComplete(result: List<HistoryRecord>) {
            Log.i("SearchApiExample", "History records: $result")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Unable to retrieve history records", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = historyDataProvider.getAll(callback)
    }

    override fun onDestroy() {
        task.cancel()
        super.onDestroy()
    }
}
