package com.mapbox.search.result

import com.mapbox.search.RequestOptions
import com.mapbox.search.common.assertDebug
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class GeocodingCompatSearchSuggestion(
    override val originalSearchResult: OriginalSearchResult,
    override val requestOptions: RequestOptions
) : BaseSearchSuggestion() {

    init {
        assertDebug(originalSearchResult.action == null && originalSearchResult.center != null) {
            "Illegal geocoding search result. " +
                    "`Action`: ${originalSearchResult.action}, " +
                    "`center`: ${originalSearchResult.center}"
        }
    }

    @IgnoredOnParcel
    val searchResultType: SearchResultType = checkNotNull(originalSearchResult.type.tryMapToSearchResultType())

    @IgnoredOnParcel
    override val type: SearchSuggestionType = SearchSuggestionType.SearchResultSuggestion(listOf(searchResultType))

    override val isBatchResolveSupported: Boolean
        get() = true

    override fun toString(): String {
        return super.toString()
    }
}
