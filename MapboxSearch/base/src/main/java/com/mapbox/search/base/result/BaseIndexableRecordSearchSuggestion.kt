package com.mapbox.search.base.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.record.BaseIndexableRecord
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseIndexableRecordSearchSuggestion(
    val record: BaseIndexableRecord,
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: BaseRequestOptions
) : BaseSearchSuggestion(rawSearchResult) {

    init {
        check(rawSearchResult.type == BaseRawResultType.USER_RECORD)
        checkNotNull(rawSearchResult.layerId)
    }

    override val id: String
        get() = rawSearchResult.userRecordId ?: rawSearchResult.id

    override val name: String
        get() = record.name

    override val coordinate: Point
        get() = rawSearchResult.center ?: record.coordinate

    override val routablePoints: List<CoreRoutablePoint>?
        get() = rawSearchResult.routablePoints ?: record.routablePoints

    override val descriptionText: String?
        get() = rawSearchResult.descriptionAddress ?: record.descriptionText

    override val address: BaseSearchAddress?
        get() = rawSearchResult.addresses?.first() ?: record.address

    override val categories: List<String>?
        get() = rawSearchResult.categories ?: record.categories

    override val makiIcon: String?
        get() = rawSearchResult.icon ?: record.makiIcon

    override val metadata: CoreResultMetadata?
        get() = rawSearchResult.metadata ?: record.metadata

    override val type: BaseSearchSuggestionType.IndexableRecordItem
        get() = BaseSearchSuggestionType.IndexableRecordItem(record, rawSearchResult.layerId!!)
}
