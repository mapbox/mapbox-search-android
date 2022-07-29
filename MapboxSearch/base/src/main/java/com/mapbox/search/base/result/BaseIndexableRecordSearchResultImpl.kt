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
        get() = record.id

    override val name: String
        get() = record.name

    override val descriptionText: String?
        get() = record.descriptionText

    override val address: BaseSearchAddress?
        get() = record.address ?: rawSearchResult.addresses?.get(0)

    override val coordinate: Point?
        get() = record.coordinate ?: rawSearchResult.center

    override val routablePoints: List<CoreRoutablePoint>?
        get() = record.routablePoints

    // TODO(search-sdk/#526): consider multiple types for IndexableRecord
    override val types: List<BaseSearchResultType>
        get() = listOf(record.type)

    override val categories: List<String>
        get() = record.categories ?: super.categories

    override val makiIcon: String?
        get() = record.makiIcon

    override val metadata: CoreResultMetadata?
        get() = record.metadata

    override fun toString(): String {
        return super.toString()
    }
}
