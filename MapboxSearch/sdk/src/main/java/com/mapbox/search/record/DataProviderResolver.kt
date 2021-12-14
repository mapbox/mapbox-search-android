package com.mapbox.search.record

internal fun interface DataProviderResolver {
    fun getRecordsLayer(name: String): IndexableDataProvider<*>?
}
