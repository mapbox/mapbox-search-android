package com.mapbox.search.base.result

import com.mapbox.search.base.BaseRequestOptions
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseServerSearchSuggestion(
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: BaseRequestOptions,
) : BaseSearchSuggestion(rawSearchResult) {

    init {
        check(rawSearchResult.action != null)
        check(rawSearchResult.type != BaseRawResultType.CATEGORY || rawSearchResult.categoryCanonicalName != null)
    }

    @IgnoredOnParcel
    override val type: BaseSearchSuggestionType = when {
        rawSearchResult.types.isValidMultiType() && rawSearchResult.types.all { it.isSearchResultType } -> {
            val searchResultTypes = rawSearchResult.types.map { it.tryMapToSearchResultType()!! }
            BaseSearchSuggestionType.SearchResultSuggestion(searchResultTypes)
        }
        rawSearchResult.type == BaseRawResultType.CATEGORY -> {
            BaseSearchSuggestionType.Category(requireNotNull(rawSearchResult.categoryCanonicalName))
        }
        rawSearchResult.type == BaseRawResultType.QUERY -> BaseSearchSuggestionType.Query
        else -> error("Illegal raw search result type: ${rawSearchResult.type}")
    }

    override fun toString(): String {
        return super.toString()
    }
}
