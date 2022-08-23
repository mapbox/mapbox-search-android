package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.result.BaseRawSearchResult
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ServerSearchResultImpl(
    override val types: List<SearchResultType>,
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: RequestOptions
) : AbstractSearchResult(rawSearchResult), ServerSearchResult {

    init {
        assertDebug(rawSearchResult.center != null) {
            "Server search result must have a coordinate"
        }
        assertDebug(types.isNotEmpty()) { "Provided types should not be empty!" }
    }

    override val coordinate: Point
        get() = requireNotNull(rawSearchResult.center)

    override fun toString(): String {
        return super.toString()
    }
}
