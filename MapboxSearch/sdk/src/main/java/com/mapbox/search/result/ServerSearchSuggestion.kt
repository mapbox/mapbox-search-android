package com.mapbox.search.result

import com.mapbox.search.RequestOptions
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ServerSearchSuggestion(
    override val originalSearchResult: OriginalSearchResult,
    override val requestOptions: RequestOptions,
    private val isFromOffline: Boolean = false
) : BaseSearchSuggestion(originalSearchResult) {

    init {
        check(isFromOffline || originalSearchResult.action != null)
        check(originalSearchResult.type != OriginalResultType.CATEGORY || originalSearchResult.categoryCanonicalName != null)
    }

    @IgnoredOnParcel
    override val type: SearchSuggestionType = when {
        originalSearchResult.types.isValidMultiType() && originalSearchResult.types.all { it.isSearchResultType } -> {
            val searchResultTypes = originalSearchResult.types.map { it.tryMapToSearchResultType()!! }
            SearchSuggestionType.SearchResultSuggestion(searchResultTypes)
        }
        originalSearchResult.type == OriginalResultType.CATEGORY -> {
            SearchSuggestionType.Category(requireNotNull(originalSearchResult.categoryCanonicalName))
        }
        originalSearchResult.type == OriginalResultType.QUERY -> SearchSuggestionType.Query
        else -> error("Illegal original search result type: ${originalSearchResult.type}")
    }

    override fun toString(): String {
        return super.toString()
    }
}
