package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.base.result.BaseIndexableRecordSearchResultImpl
import com.mapbox.search.base.result.BaseIndexableRecordSearchSuggestion
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.mapToBase
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

internal fun ResponseInfo.fixNonDeterminedFields(fixedSessionID: String): ResponseInfo {
    return ResponseInfo(
        requestOptions = requestOptions.copy(sessionID = fixedSessionID),
        coreSearchResponse = coreSearchResponse,
        isReproducible = isReproducible,
    )
}

internal fun SearchSuggestion.fixNonDeterminedFields(userRecordPriority: Int, sessionID: String): SearchSuggestion {
    val fixedRawSearchResult = base.rawSearchResult.copy(userRecordPriority = userRecordPriority)
    val fixedRequestOptions = requestOptions.copy(sessionID = sessionID).mapToBase()
    val base = when (base) {
        is BaseServerSearchSuggestion -> {
            base.copy(rawSearchResult = fixedRawSearchResult, requestOptions = fixedRequestOptions)
        }
        is BaseIndexableRecordSearchSuggestion -> {
            base.copy(rawSearchResult = fixedRawSearchResult, requestOptions = fixedRequestOptions)
        }
        else -> throw IllegalStateException("Unknown type of $javaClass")
    }
    return SearchSuggestion(base)
}

internal fun SearchResult.fixNonDeterminedFields(userRecordPriority: Int, sessionID: String): SearchResult {
    val fixedBaseRawSearchResult = base.rawSearchResult.copy(userRecordPriority = userRecordPriority)
    val fixedRequestOptions = requestOptions.copy(sessionID = sessionID).mapToBase()
    val base = when (base) {
        is BaseServerSearchResultImpl -> {
            base.copy(rawSearchResult = fixedBaseRawSearchResult, requestOptions = fixedRequestOptions)
        }
        is BaseIndexableRecordSearchResultImpl -> {
            base.copy(rawSearchResult = fixedBaseRawSearchResult, requestOptions = fixedRequestOptions)
        }
        else -> throw IllegalStateException("Unknown type of $javaClass")
    }
    return SearchResult(base)
}

internal fun compareSearchResultWithServerSearchResult(
    expected: SearchResult,
    serverResult: SearchResult
): Boolean {
    if (expected === serverResult) return true
    if (expected.javaClass != serverResult.javaClass) return false

    val fixedResult = expected.fixNonDeterminedFields(
        serverResult.base.rawSearchResult.userRecordPriority,
        serverResult.requestOptions.sessionID
    )
    return fixedResult == serverResult
}

internal fun compareSearchResultWithServerSearchResult(
    expected: SearchSuggestion,
    serverResult: SearchSuggestion
): Boolean {
    if (expected === serverResult) return true
    if (expected.javaClass != serverResult.javaClass) return false

    val fixedResult = expected.fixNonDeterminedFields(
        serverResult.base.rawSearchResult.userRecordPriority,
        serverResult.requestOptions.sessionID
    )
    return fixedResult == serverResult
}

internal fun compareSearchResultWithServerSearchResult(
    expected: BaseRawSearchResult,
    serverResult: BaseRawSearchResult
): Boolean {
    if (expected === serverResult) return true
    if (expected.javaClass != serverResult.javaClass) return false

    val fixedBaseRawSearchResult = expected.copy(
        userRecordPriority = serverResult.userRecordPriority
    )

    return fixedBaseRawSearchResult == serverResult
}
