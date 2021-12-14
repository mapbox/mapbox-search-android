package com.mapbox.search.record

import com.google.gson.annotations.SerializedName
import com.mapbox.search.utils.serialization.HistoryRecordDAO
import com.mapbox.search.utils.serialization.RecordsSerializer

internal class HistoryRecordsSerializer : RecordsSerializer<HistoryRecord, HistoryRecordDAO, HistoryRecordsSerializer.HistoryData>() {

    override val actualDataVersion: Int = CURRENT_VERSION_DATA

    override fun createRecord(records: List<HistoryRecord>): HistoryData {
        return HistoryData(version = CURRENT_VERSION_DATA, records = records.map { HistoryRecordDAO.create(it) })
    }

    override fun restoreRecord(json: String): HistoryData {
        if (json.isEmpty()) {
            return HistoryData(CURRENT_VERSION_DATA, emptyList())
        }
        return gson.fromJson<HistoryData>(json, HistoryData::class.java)
    }

    internal data class HistoryData(
        @SerializedName("version") override val version: Int,
        @SerializedName("records") override val records: List<HistoryRecordDAO>
    ) : RecordsData<HistoryRecordDAO>

    private companion object {
        const val CURRENT_VERSION_DATA = 0
    }
}
