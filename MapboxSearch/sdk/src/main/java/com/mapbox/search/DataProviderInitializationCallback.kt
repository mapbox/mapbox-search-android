package com.mapbox.search

import com.mapbox.search.record.IndexableDataProvider

/**
 * Interface definition for a callback to be invoked when default data provider has been initialized and is ready to use.
 */
public interface DataProviderInitializationCallback {

    /**
     * Invoked when [dataProvider] has been initialized successfully.
     *
     * @param dataProvider [IndexableDataProvider] that is ready to ise.
     */
    public fun onInitialized(dataProvider: IndexableDataProvider<*>)

    /**
     * Invoked when an error has occurred during [dataProvider] initialization.
     *
     * @param dataProvider [IndexableDataProvider], for which initialization error has occurred.
     * @param e [Exception], occurred during operation.
     */
    public fun onError(dataProvider: IndexableDataProvider<*>, e: Exception)
}
