package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.geojson.Point
import com.mapbox.search.record.FavoriteRecord

internal data class FavoriteRecordDAO(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("coordinate") val coordinate: Point? = null,
    @SerializedName("descriptionText") val descriptionText: String? = null,
    @SerializedName("address") val address: SearchAddressDAO? = null,
    @SerializedName("searchResultType") val searchResultType: SearchResultTypeDAO? = null,
    @SerializedName("makiIcon") val makiIcon: String? = null,
    @SerializedName("categories") val categories: List<String>? = null,
    @SerializedName("routablePoints") val routablePoints: List<RoutablePointDAO>? = null,
    @SerializedName("metadata") val metadata: SearchResultMetadataDAO? = null,
) : DataAccessObject<FavoriteRecord> {

    override val isValid: Boolean
        get() = id != null && name != null && coordinate != null &&
                address?.isValid != false && searchResultType?.isValid == true &&
                routablePoints?.all { it.isValid } != false && metadata?.isValid != false

    override fun createData(): FavoriteRecord {
        return FavoriteRecord(
            id = id!!,
            name = name!!,
            coordinate = coordinate!!,
            descriptionText = descriptionText,
            address = address?.createData(),
            type = searchResultType!!.createData(),
            makiIcon = makiIcon,
            categories = categories,
            routablePoints = routablePoints?.map { it.createData() },
            metadata = metadata?.createData()
        )
    }

    companion object {

        fun create(favoriteRecord: FavoriteRecord): FavoriteRecordDAO {
            return with(favoriteRecord) {
                FavoriteRecordDAO(
                    id = id,
                    name = name,
                    coordinate = coordinate,
                    descriptionText = descriptionText,
                    address = SearchAddressDAO.create(address),
                    searchResultType = SearchResultTypeDAO.create(favoriteRecord.type),
                    makiIcon = makiIcon,
                    categories = categories,
                    routablePoints = routablePoints?.mapNotNull { RoutablePointDAO.create(it) },
                    metadata = SearchResultMetadataDAO.create(metadata)
                )
            }
        }
    }
}
