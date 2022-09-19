package com.mapbox.search.utils.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mapbox.geojson.Point
import com.mapbox.search.base.logger.logw

internal abstract class RecordsSerializer<DATA, DAO : DataAccessObject<DATA>, RECORDS : RecordsSerializer.RecordsData<DAO>> {

    protected open val gson: Gson = with(GsonBuilder()) {
        registerTypeAdapter(Point::class.java, PointTypeAdapter().nullSafe())
        create()
    }

    protected abstract val actualDataVersion: Int

    protected abstract fun createRecord(records: List<DATA>): RECORDS
    protected abstract fun restoreRecord(json: String): RECORDS

    fun serialize(records: List<DATA>): ByteArray {
        val data = createRecord(records)
        return gson.toJson(data).toByteArray(Charsets.UTF_8)
    }

    fun deserialize(data: ByteArray, skipIncorrectEntries: Boolean = true): List<DATA> {
        val json = String(data, Charsets.UTF_8)
        val recordsData = restoreRecord(json)
        if (recordsData.version != actualDataVersion) {
            // Currently we have only one version, so nothing to migrate here
            throw IllegalStateException("Unsupported data version ${recordsData.version}")
        }

        return recordsData.records.asSequence()
            .filter {
                val isValid = !skipIncorrectEntries || it.isValid
                if (!isValid) {
                    logw("$it is not valid. Skipping it")
                }
                isValid
            }
            .map { it.createData() }
            .toList()
    }

    interface RecordsData<DAO> {
        val version: Int
        val records: List<DAO>
    }
}
