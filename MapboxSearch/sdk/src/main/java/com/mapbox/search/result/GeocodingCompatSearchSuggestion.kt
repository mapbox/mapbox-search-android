package com.mapbox.search.result

import com.mapbox.search.RequestOptions
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.result.BaseRawSearchResult
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class GeocodingCompatSearchSuggestion(
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: RequestOptions
) : AbstractSearchSuggestion(rawSearchResult) {

    init {
        assertDebug(rawSearchResult.action == null && rawSearchResult.center != null) {
            "Illegal geocoding search result. " +
                    "`Action`: ${rawSearchResult.action}, " +
                    "`center`: ${rawSearchResult.center}"
        }
    }

    @IgnoredOnParcel
    val searchResultType: SearchResultType = checkNotNull(rawSearchResult.type.tryMapToSearchResultType()).mapToPlatform()

    @IgnoredOnParcel
    override val type: SearchSuggestionType = SearchSuggestionType.SearchResultSuggestion(listOf(searchResultType))

    override val isBatchResolveSupported: Boolean
        get() = true

    override fun toString(): String {
        return super.toString()
    }
}
