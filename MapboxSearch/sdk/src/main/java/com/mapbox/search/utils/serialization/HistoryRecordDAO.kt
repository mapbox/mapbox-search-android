package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.geojson.Point
import com.mapbox.search.record.HistoryRecord

internal data class HistoryRecordDAO(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("coordinate") val coordinate: Point? = null,
    @SerializedName("descriptionText") val descriptionText: String? = null,
    @SerializedName("address") val address: SearchAddressDAO? = null,
    @SerializedName("timestamp") val timestamp: Long? = null,
    @SerializedName("searchResultType") val searchResultType: SearchResultTypeDAO? = null,
    @SerializedName("makiIcon") val makiIcon: String? = null,
    @SerializedName("categories") val categories: List<String>? = null,
    @SerializedName("routablePoints") val routablePoints: List<RoutablePointDAO>? = null,
    @SerializedName("metadata") val metadata: SearchResultMetadataDAO? = null,
) : DataAccessObject<HistoryRecord> {

    override val isValid: Boolean
        get() = id != null && name != null && timestamp != null &&
                address?.isValid != false && searchResultType?.isValid == true &&
                routablePoints?.all { it.isValid } != false && metadata?.isValid != false

    override fun createData(): HistoryRecord {
        return HistoryRecord(
            id = id!!,
            name = name!!,
            descriptionText = descriptionText,
            address = address?.createData(),
            routablePoints = routablePoints?.map { it.createData() },
            categories = categories,
            makiIcon = makiIcon,
            coordinate = coordinate,
            type = searchResultType!!.createData(),
            metadata = metadata?.createData(),
            timestamp = timestamp!!,
        )
    }

    companion object {

        fun create(historyRecord: HistoryRecord): HistoryRecordDAO {
            return with(historyRecord) {
                HistoryRecordDAO(
                    id = id,
                    name = name,
                    coordinate = coordinate,
                    descriptionText = descriptionText,
                    address = SearchAddressDAO.create(address),
                    timestamp = timestamp,
                    makiIcon = makiIcon,
                    categories = categories,
                    searchResultType = SearchResultTypeDAO.create(type),
                    routablePoints = routablePoints?.mapNotNull { RoutablePointDAO.create(it) },
                    metadata = SearchResultMetadataDAO.create(metadata)
                )
            }
        }
    }
}
