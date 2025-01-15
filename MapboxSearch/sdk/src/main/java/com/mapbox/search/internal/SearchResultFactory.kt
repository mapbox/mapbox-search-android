package com.mapbox.search.internal

import androidx.annotation.RestrictTo
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.result.SearchResult

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public object SearchResultFactory {
    public fun create(base: BaseSearchResult): SearchResult = SearchResult(base)
}