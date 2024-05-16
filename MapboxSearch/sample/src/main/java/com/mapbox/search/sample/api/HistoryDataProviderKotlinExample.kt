package com.mapbox.search.sample.api

import com.mapbox.search.ServiceProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.sample.R

class HistoryDataProviderKotlinExample : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_history_data_provider_kt_example

    private val historyDataProvider = ServiceProvider.INSTANCE.historyDataProvider()

    private var task: AsyncOperationTask? = null

    private val callback: CompletionCallback<List<HistoryRecord>> = object : CompletionCallback<List<HistoryRecord>> {
        override fun onComplete(result: List<HistoryRecord>) {
            logI("SearchApiExample", "History records:", result)
            onFinished()
        }

        override fun onError(e: Exception) {
            logI("SearchApiExample", "Unable to retrieve history records", e)
            onFinished()
        }
    }

    override fun startExample() {
        task = historyDataProvider.getAll(callback)
    }

    override fun onDestroy() {
        task?.cancel()
        super.onDestroy()
    }
}
