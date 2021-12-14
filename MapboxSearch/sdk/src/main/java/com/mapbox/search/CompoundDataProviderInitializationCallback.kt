package com.mapbox.search

import com.mapbox.search.record.IndexableDataProvider

internal class CompoundDataProviderInitializationCallback : DataProviderInitializationCallback {

    @Volatile
    var callbacks: List<DataProviderInitializationCallback> = emptyList()
        private set

    private var results = HashMap<IndexableDataProvider<*>, Result<Unit>>()

    internal fun addCallback(callback: DataProviderInitializationCallback) {
        var results: Map<IndexableDataProvider<*>, Result<Unit>>

        synchronized(this) {
            results = this.results.toMap()
            callbacks = callbacks + callback
        }

        results.forEach { (provider, result) ->
            if (result.isSuccess) {
                callback.onInitialized(provider)
            } else {
                callback.onError(provider, result.exceptionOrNull() as Exception)
            }
        }
    }

    internal fun removeCallback(callback: DataProviderInitializationCallback) {
        synchronized(this) {
            callbacks = callbacks - callback
        }
    }

    internal fun removeAllCallbacks() {
        synchronized(this) {
            callbacks = emptyList()
        }
    }

    override fun onInitialized(dataProvider: IndexableDataProvider<*>) {
        var callbacks: List<DataProviderInitializationCallback>

        synchronized(this) {
            results[dataProvider] = Result.success(Unit)
            callbacks = this.callbacks
        }

        callbacks.forEach { callback ->
            callback.onInitialized(dataProvider)
        }
    }

    override fun onError(dataProvider: IndexableDataProvider<*>, e: Exception) {
        var callbacks: List<DataProviderInitializationCallback>

        synchronized(this) {
            results[dataProvider] = Result.failure(e)
            callbacks = this.callbacks
        }

        callbacks.forEach { callback ->
            callback.onError(dataProvider, e)
        }
    }
}
