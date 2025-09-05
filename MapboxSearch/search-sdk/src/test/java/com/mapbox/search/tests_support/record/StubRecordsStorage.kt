package com.mapbox.search.tests_support.record

import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.record.RecordsStorage

internal class StubRecordsStorage<R : IndexableRecord>(
    records: List<R> = emptyList()
) : RecordsStorage<R> {

    val records: MutableList<R> = records.toMutableList()

    override fun load(): List<R> {
        return records
    }

    override fun save(records: List<R>) {
        this.records.clear()
        this.records.addAll(records)
    }
}
