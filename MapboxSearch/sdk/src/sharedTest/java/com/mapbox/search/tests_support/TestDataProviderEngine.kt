package com.mapbox.search.tests_support

import com.mapbox.search.record.IndexableDataProviderEngine
import com.mapbox.search.record.IndexableDataProviderEngine.BatchUpdateOperation
import com.mapbox.search.record.IndexableRecord

internal class TestDataProviderEngine<R : IndexableRecord> : IndexableDataProviderEngine {

    private var _records: MutableList<IndexableRecord> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    val records: List<R>
        get() = _records.toList() as List<R>

    override fun add(record: IndexableRecord) {
        _records.add(record)
    }

    override fun addAll(records: Iterable<IndexableRecord>) {
        this._records.addAll(records)
    }

    override fun update(record: IndexableRecord) {
        _records.replaceAll {
            if (record.id == it.id) {
                record
            } else {
                it
            }
        }
    }

    override fun remove(id: String) {
        _records.removeAll { it.id == id }
    }

    override fun removeAll(ids: Iterable<String>) {
        val idsSet = ids.toSet()
        _records.removeAll { idsSet.contains(it.id) }
    }

    override fun clear() {
        _records.clear()
    }

    override fun executeBatchUpdate(batchUpdateOperation: BatchUpdateOperation) {
        batchUpdateOperation.execute(this)
    }
}
