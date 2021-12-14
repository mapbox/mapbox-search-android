package com.mapbox.search.record

import com.google.gson.annotations.SerializedName
import com.mapbox.search.utils.serialization.FavoriteRecordDAO
import com.mapbox.search.utils.serialization.RecordsSerializer

internal class FavoriteRecordsSerializer : RecordsSerializer<FavoriteRecord, FavoriteRecordDAO, FavoriteRecordsSerializer.FavoritesData>() {

    override val actualDataVersion: Int = CURRENT_VERSION_DATA

    override fun createRecord(records: List<FavoriteRecord>): FavoritesData {
        return FavoritesData(version = CURRENT_VERSION_DATA, records = records.map { FavoriteRecordDAO.create(it) })
    }

    override fun restoreRecord(json: String): FavoritesData {
        if (json.isEmpty()) {
            return FavoritesData(CURRENT_VERSION_DATA, emptyList())
        }
        return gson.fromJson<FavoritesData>(json, FavoritesData::class.java)
    }

    internal data class FavoritesData(
        @SerializedName("version") override val version: Int,
        @SerializedName("records") override val records: List<FavoriteRecordDAO>
    ) : RecordsData<FavoriteRecordDAO>

    private companion object {
        const val CURRENT_VERSION_DATA = 0
    }
}
