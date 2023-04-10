package com.mapbox.search.common.tests

import com.mapbox.search.internal.bindgen.SearchResult

fun compareSearchResultWithServerSearchResult(expected: SearchResult, serverResult: SearchResult): Boolean {
    if (expected == serverResult) return true
    val fixedResult = expected.copy(userRecordPriority = serverResult.userRecordPriority)
    return fixedResult == serverResult
}
