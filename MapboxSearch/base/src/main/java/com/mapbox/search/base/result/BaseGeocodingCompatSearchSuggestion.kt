package com.mapbox.search.base.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.assertDebug
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseGeocodingCompatSearchSuggestion(
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: BaseRequestOptions
) : BaseSearchSuggestion(rawSearchResult) {

    init {
        assertDebug(rawSearchResult.action == null && rawSearchResult.center != null) {
            "Illegal geocoding search result. " +
                    "`Action`: ${rawSearchResult.action}, " +
                    "`center`: ${rawSearchResult.center}"
        }
    }

    override val coordinate: Point
        get() = requireNotNull(rawSearchResult.center)

    @IgnoredOnParcel
    val searchResultType: BaseSearchResultType = checkNotNull(rawSearchResult.type.tryMapToSearchResultType())

    @IgnoredOnParcel
    override val type: BaseSearchSuggestionType =
        BaseSearchSuggestionType.SearchResultSuggestion(listOf(searchResultType))

    override val isBatchResolveSupported: Boolean
        get() = true
}
