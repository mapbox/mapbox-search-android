package com.mapbox.search.record

import com.mapbox.search.base.core.CoreUserRecordsLayer

/**
 * Provides a mechanism for search index changes in core layers.
 * One core layer can be associated with several [search engines][com.mapbox.search.SearchEngine].
 *
 * Please note that even though in most cases execution of each operation is pretty fast,
 * in some cases (when core layer is currently used by [search engine][com.mapbox.search.SearchEngine]
 * or when >10000 records are being added) execution may take some time. Please, consider usage of this class
 * from background thread.
 */
public interface IndexableDataProviderEngine {

    /**
     * Insert or update existing [IndexableRecord] to search index.
     *
     * @param record record to be added.
     */
    public fun upsert(record: IndexableRecord)

    /**
     * Adds a bunch of [IndexableRecord] to search index.
     *
     * @param records records to be added.
     */
    public fun upsertAll(records: Iterable<IndexableRecord>)

    /**
     * Removes [IndexableRecord] with specified [id] from search index.
     *
     * @param id ID of the record, that should be removed.
     */
    public fun remove(id: String)

    /**
     * Removes a bunch of [IndexableRecord] with specified [ids] from search index.
     *
     * @param ids IDs of records, that should be removed.
     */
    public fun removeAll(ids: Iterable<String>)

    /**
     * Clears the whole search index.
     */
    public fun clear()
}

internal class IndexableDataProviderEngineImpl internal constructor(
    val coreLayer: CoreUserRecordsLayer
) : IndexableDataProviderEngine {

    override fun upsert(record: IndexableRecord) {
        val coreRecord = record.mapToCore()
        coreLayer.upsert(coreRecord)
    }

    override fun upsertAll(records: Iterable<IndexableRecord>) {
        val coreRecords = records.map { it.mapToCore() }
        coreLayer.upsertMulti(coreRecords)
    }

    override fun remove(id: String) {
        coreLayer.remove(id)
    }

    override fun removeAll(ids: Iterable<String>) {
        coreLayer.removeMulti(ids.toList())
    }

    override fun clear() {
        coreLayer.clear()
    }
}
