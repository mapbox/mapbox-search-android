package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.record.IndexableRecord
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class IndexableRecordSearchResultImpl(
    override val record: IndexableRecord,
    override val originalSearchResult: OriginalSearchResult,
    override val requestOptions: RequestOptions
) : BaseSearchResult(originalSearchResult), IndexableRecordSearchResult {

    override val id: String
        get() = record.id

    override val name: String
        get() = record.name

    override val descriptionText: String?
        get() = record.descriptionText

    override val address: SearchAddress?
        get() = record.address ?: originalSearchResult.addresses?.get(0)

    override val coordinate: Point?
        get() = record.coordinate ?: originalSearchResult.center

    override val routablePoints: List<RoutablePoint>?
        get() = record.routablePoints

    // TODO(search-sdk/#526): consider multiple types for IndexableRecord
    override val types: List<SearchResultType>
        get() = listOf(record.type)

    override val categories: List<String>
        get() = record.categories ?: super.categories

    override val makiIcon: String?
        get() = record.makiIcon

    override val metadata: SearchResultMetadata?
        get() = record.metadata

    override fun toString(): String {
        return super.toString()
    }
}
