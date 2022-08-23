package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngine
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.sample.BuildConfig
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.Executor

class CustomIndexableDataProviderKotlinExample : AppCompatActivity() {

    private lateinit var searchEngine: SearchEngine
    private lateinit var registerProviderTask: AsyncOperationTask
    private var searchRequestTask: AsyncOperationTask? = null

    private val customDataProvider = InMemoryDataProvider(
        records = listOf(
            createRecord("Let it be", Point.fromLngLat(27.575321258282806, 53.89025545661358)),
            createRecord("Laŭka", Point.fromLngLat(27.574862357961212, 53.88998973246244)),
            createRecord("Underdog", Point.fromLngLat(27.57573285942709, 53.89020312748444)),
        )
    )

    private val searchCallback = object : SearchSelectionCallback {

        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
            if (suggestions.isEmpty()) {
                Log.i("SearchApiExample", "No suggestions found")
            } else {
                Log.i("SearchApiExample", "Search suggestions: $suggestions.\nSelecting first suggestion...")
                searchRequestTask = searchEngine.select(suggestions.first(), this)
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Search result: $result")
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Category search results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchEngine = MapboxSearchSdk.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(BuildConfig.MAPBOX_API_TOKEN)
        )

        Log.i("SearchApiExample", "Start CustomDataProvider registering...")
        registerProviderTask = searchEngine.registerDataProvider(
            dataProvider = customDataProvider,
            callback = object : CompletionCallback<Unit> {
                override fun onComplete(result: Unit) {
                    Log.i("SearchApiExample", "CustomDataProvider is registered")
                    searchRequestTask = searchEngine.search(
                        "Underdog",
                        SearchOptions(
                            proximity = Point.fromLngLat(27.574862357961212, 53.88998973246244),
                        ),
                        searchCallback
                    )
                }

                override fun onError(e: Exception) {
                    Log.i("SearchApiExample", "Error during registering", e)
                }
            }
        )
    }

    override fun onDestroy() {
        registerProviderTask.cancel()
        searchRequestTask?.cancel()
        searchEngine.unregisterDataProvider(
            dataProvider = customDataProvider,
            callback = object : CompletionCallback<Unit> {
                override fun onComplete(result: Unit) {
                    Log.i("SearchApiExample", "CustomDataProvider is unregistered")
                }

                override fun onError(e: Exception) {
                    Log.i("SearchApiExample", "Error during unregistering", e)
                }
            }
        )
        super.onDestroy()
    }

    private fun createRecord(name: String, coordinate: Point): IndexableRecord {
        return FavoriteRecord(
            UUID.randomUUID().toString(),
            name,
            null,
            null,
            null,
            emptyList(),
            null,
            coordinate,
            SearchResultType.POI,
            null,
        )
    }

    private class InMemoryDataProvider<R : IndexableRecord>(records: List<R>) : IndexableDataProvider<R> {

        private val dataProviderEngines: MutableList<IndexableDataProviderEngine> = mutableListOf()
        private val records: MutableMap<String, R> = mutableMapOf()

        override val dataProviderName: String = "SAMPLE_APP_CUSTOM_DATA_PROVIDER"
        override val priority: Int = 200

        init {
            this.records.putAll(records.map { it.id to it })
        }

        override fun registerIndexableDataProviderEngine(
            dataProviderEngine: IndexableDataProviderEngine,
            executor: Executor,
            callback: CompletionCallback<Unit>
        ): AsyncOperationTask {
            dataProviderEngine.upsertAll(records.values.toList())
            dataProviderEngines.add(dataProviderEngine)
            executor.execute {
                callback.onComplete(Unit)
            }
            return CompletedAsyncOperationTask
        }

        override fun unregisterIndexableDataProviderEngine(
            dataProviderEngine: IndexableDataProviderEngine,
            executor: Executor,
            callback: CompletionCallback<Boolean>
        ): AsyncOperationTask {
            val isRemoved = dataProviderEngines.remove(dataProviderEngine)
            if (isRemoved) {
                dataProviderEngine.clear()
            }
            executor.execute {
                callback.onComplete(isRemoved)
            }
            return CompletedAsyncOperationTask
        }

        override operator fun get(
            id: String,
            executor: Executor,
            callback: CompletionCallback<in R?>
        ): AsyncOperationTask {
            executor.execute {
                callback.onComplete(records[id])
            }
            return CompletedAsyncOperationTask
        }

        override fun getAll(executor: Executor, callback: CompletionCallback<List<R>>): AsyncOperationTask {
            executor.execute {
                callback.onComplete(ArrayList(records.values))
            }
            return CompletedAsyncOperationTask
        }

        override fun contains(
            id: String,
            executor: Executor,
            callback: CompletionCallback<Boolean>
        ): AsyncOperationTask {
            executor.execute {
                callback.onComplete(records[id] != null)
            }
            return CompletedAsyncOperationTask
        }

        override fun upsert(record: R, executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask {
            dataProviderEngines.forEach {
                it.upsert(record)
            }
            records[record.id] = record
            executor.execute {
                callback.onComplete(Unit)
            }
            return CompletedAsyncOperationTask
        }

        override fun upsertAll(
            records: List<R>,
            executor: Executor,
            callback: CompletionCallback<Unit>
        ): AsyncOperationTask {
            dataProviderEngines.forEach {
                it.upsertAll(records)
            }
            for (record in records) {
                this.records[record.id] = record
            }
            executor.execute {
                callback.onComplete(Unit)
            }
            return CompletedAsyncOperationTask
        }

        override fun remove(id: String, executor: Executor, callback: CompletionCallback<Boolean>): AsyncOperationTask {
            dataProviderEngines.forEach {
                it.remove(id)
            }
            val isRemoved = records.remove(id) != null
            executor.execute {
                callback.onComplete(isRemoved)
            }
            return CompletedAsyncOperationTask
        }

        override fun clear(executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask {
            dataProviderEngines.forEach {
                it.clear()
            }
            records.clear()
            executor.execute {
                callback.onComplete(Unit)
            }
            return CompletedAsyncOperationTask
        }
    }

    private object CompletedAsyncOperationTask : AsyncOperationTask {

        override val isDone: Boolean
            get() = true

        override val isCancelled: Boolean
            get() = false

        override fun cancel() {
            // Do nothing
        }
    }
}
