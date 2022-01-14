package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.common.assertDebug
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ServerSearchResultImpl(
    override val types: List<SearchResultType>,
    override val originalSearchResult: OriginalSearchResult,
    override val requestOptions: RequestOptions
) : BaseSearchResult(originalSearchResult), ServerSearchResult {

    init {
        assertDebug(originalSearchResult.center != null) {
            "Server search result must have a coordinate"
        }
        assertDebug(types.isNotEmpty()) { "Provided types should not be empty!" }
    }

    override val coordinate: Point
        get() = requireNotNull(originalSearchResult.center)

    override fun toString(): String {
        return super.toString()
    }
}
