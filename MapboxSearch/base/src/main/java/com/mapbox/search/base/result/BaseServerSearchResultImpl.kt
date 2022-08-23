package com.mapbox.search.base.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.assertDebug
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseServerSearchResultImpl(
    override val types: List<BaseSearchResultType>,
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: BaseRequestOptions
) : BaseSearchResult(rawSearchResult) {

    init {
        assertDebug(rawSearchResult.center != null) {
            "Server search result must have a coordinate"
        }
        assertDebug(types.isNotEmpty()) { "Provided types should not be empty!" }
    }

    @IgnoredOnParcel
    override val baseType: Type
        get() = Type.ServerResult(coordinate)

    override val coordinate: Point
        get() = requireNotNull(rawSearchResult.center)
}
