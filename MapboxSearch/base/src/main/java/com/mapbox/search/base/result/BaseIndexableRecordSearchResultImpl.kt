package com.mapbox.search.base.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.record.BaseIndexableRecord
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseIndexableRecordSearchResultImpl(
    val record: BaseIndexableRecord,
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: BaseRequestOptions
) : BaseSearchResult(rawSearchResult) {

    @IgnoredOnParcel
    override val baseType: Type = Type.IndexableRecordSearchResult(record)

    override val id: String
        get() = rawSearchResult.userRecordId ?: rawSearchResult.id

    override val name: String
        get() = record.name

    override val descriptionText: String?
        get() = rawSearchResult.descriptionAddress ?: record.descriptionText

    override val address: BaseSearchAddress?
        get() = rawSearchResult.addresses?.first() ?: record.address

    override val coordinate: Point
        get() = rawSearchResult.center ?: record.coordinate

    override val routablePoints: List<CoreRoutablePoint>?
        get() = rawSearchResult.routablePoints ?: record.routablePoints

    override val types: List<BaseSearchResultType>
        get() = listOf(record.type)

    override val categories: List<String>?
        get() = rawSearchResult.categories ?: record.categories

    override val makiIcon: String?
        get() = rawSearchResult.icon ?: record.makiIcon

    override val metadata: CoreResultMetadata?
        get() = rawSearchResult.metadata ?: record.metadata
}
