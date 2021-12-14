package com.mapbox.search.record

import com.mapbox.search.core.CoreUserRecordsLayer
import com.mapbox.search.record.IndexableDataProviderEngineLayer.BatchUpdateOperation
import com.mapbox.search.utils.SyncLocker

/**
 * Provides a mechanism for search index changes in core layers.
 * One core layer can be associated with several [search engines][com.mapbox.search.SearchEngine].
 *
 * Please note that even though in most cases execution of each operation is pretty fast,
 * in some cases (when core layer is currently used by [search engine][com.mapbox.search.SearchEngine]
 * or when >10000 records are being added) execution may take some time. Please, consider usage of this class
 * from background thread.
 */
public interface IndexableDataProviderEngineLayer {

    /**
     * Adds [IndexableRecord] to search index.
     *
     * @param record record to be added.
     */
    public fun add(record: IndexableRecord)

    /**
     * Adds a bunch of [IndexableRecord] to search index.
     *
     * @param records records to be added.
     */
    public fun addAll(records: Iterable<IndexableRecord>)

    /**
     * Updates [IndexableRecord] in search index.
     *
     * @param record record to be updated.
     */
    public fun update(record: IndexableRecord)

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

    /**
     * Atomically executes all operations on engine layer.
     *
     * @param batchUpdateOperation lambda, that will be executed atomically.
     */
    public fun executeBatchUpdate(batchUpdateOperation: BatchUpdateOperation)

    /**
     * Interface definition for a function to be executed atomically during [IndexableDataProviderEngineLayer] update.
     */
    public fun interface BatchUpdateOperation {

        /**
         * Applies all modifications to [engineLayer], that should be executed atomically.
         *
         * @param engineLayer engine layer, that will be updated by this operation.
         */
        public fun execute(engineLayer: IndexableDataProviderEngineLayer)
    }
}

internal class IndexableDataProviderEngineLayerImpl private constructor(
    internal val coreLayerContext: CoreLayerContext
) : IndexableDataProviderEngineLayer {

    override fun add(record: IndexableRecord) {
        val coreRecord = record.mapToCore()
        coreLayerContext.executeInSync {
            add(coreRecord)
        }
    }

    override fun addAll(records: Iterable<IndexableRecord>) {
        val coreRecords = records.map { it.mapToCore() }
        coreLayerContext.executeInSync {
            addMulti(coreRecords)
        }
    }

    override fun update(record: IndexableRecord) {
        val coreRecord = record.mapToCore()
        coreLayerContext.executeInSync {
            update(coreRecord)
        }
    }

    override fun remove(id: String) {
        coreLayerContext.executeInSync {
            remove(id)
        }
    }

    override fun removeAll(ids: Iterable<String>) {
        coreLayerContext.executeInSync {
            removeMulti(ids.toList())
        }
    }

    override fun clear() {
        coreLayerContext.executeInSync {
            clear()
        }
    }

    override fun executeBatchUpdate(batchUpdateOperation: BatchUpdateOperation) {
        coreLayerContext.executeInSync {
            batchUpdateOperation.execute(this@IndexableDataProviderEngineLayerImpl)
        }
    }

    internal data class CoreLayerContext(
        val coreLayer: CoreUserRecordsLayer,
        val syncLocker: SyncLocker,
    ) {

        fun executeInSync(action: CoreUserRecordsLayer.() -> Unit) {
            syncLocker.executeInSync {
                coreLayer.action()
            }
        }
    }

    companion object {

        fun create(coreLayerContext: CoreLayerContext) = IndexableDataProviderEngineLayerImpl(
            coreLayerContext = coreLayerContext
        )
    }
}
