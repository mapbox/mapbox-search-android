package com.mapbox.search.result

import com.mapbox.search.RequestOptions
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.isValidMultiType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ServerSearchSuggestion(
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: RequestOptions
) : AbstractSearchSuggestion(rawSearchResult) {

    init {
        check(rawSearchResult.action != null)
        check(rawSearchResult.type != BaseRawResultType.CATEGORY || rawSearchResult.categoryCanonicalName != null)
    }

    @IgnoredOnParcel
    override val type: SearchSuggestionType = when {
        rawSearchResult.types.isValidMultiType() && rawSearchResult.types.all { it.isSearchResultType } -> {
            val searchResultTypes = rawSearchResult.types.mapNotNull { it.tryMapToSearchResultType()?.mapToPlatform() }
            SearchSuggestionType.SearchResultSuggestion(searchResultTypes)
        }
        rawSearchResult.type == BaseRawResultType.CATEGORY -> {
            SearchSuggestionType.Category(requireNotNull(rawSearchResult.categoryCanonicalName))
        }
        rawSearchResult.type == BaseRawResultType.QUERY -> SearchSuggestionType.Query
        else -> error("Illegal raw search result type: ${rawSearchResult.type}")
    }

    override fun toString(): String {
        return super.toString()
    }
}
