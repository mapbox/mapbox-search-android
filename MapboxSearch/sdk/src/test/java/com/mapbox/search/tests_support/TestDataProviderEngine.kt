package com.mapbox.search.tests_support

import com.mapbox.search.record.IndexableDataProviderEngine
import com.mapbox.search.record.IndexableRecord

internal class TestDataProviderEngine<R : IndexableRecord> : IndexableDataProviderEngine {

    private var _records: MutableList<IndexableRecord> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    val records: List<R>
        get() = _records.toList() as List<R>

    override fun upsert(record: IndexableRecord) {
        _records.remove(record)
        _records.add(record)
    }

    override fun upsertAll(records: Iterable<IndexableRecord>) {
        _records.removeAll(records)
        _records.addAll(records)
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
}
