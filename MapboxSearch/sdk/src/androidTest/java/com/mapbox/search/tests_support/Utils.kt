package com.mapbox.search.tests_support

import com.mapbox.search.ResponseInfo
import com.mapbox.search.result.BaseSearchResult
import com.mapbox.search.result.BaseSearchSuggestion
import com.mapbox.search.result.IndexableRecordSearchResultImpl
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.OriginalSearchResult
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.ServerSearchSuggestion

internal fun ResponseInfo.fixNonDeterminedFields(fixedSessionID: String): ResponseInfo {
    return ResponseInfo(
        requestOptions = requestOptions.copy(sessionID = fixedSessionID),
        coreSearchResponse = coreSearchResponse,
        isReproducible = isReproducible,
    )
}

internal fun SearchSuggestion.fixNonDeterminedFields(userRecordPriority: Int, sessionID: String): SearchSuggestion {
    this as BaseSearchSuggestion
    val fixedOriginalSearchResult = originalSearchResult.copy(userRecordPriority = userRecordPriority)
    val fixedRequestOptions = requestOptions.copy(sessionID = sessionID)
    return when (this) {
        is ServerSearchSuggestion -> {
            copy(originalSearchResult = fixedOriginalSearchResult, requestOptions = fixedRequestOptions)
        }
        is IndexableRecordSearchSuggestion -> {
            copy(originalSearchResult = fixedOriginalSearchResult, requestOptions = fixedRequestOptions)
        }
        else -> throw IllegalStateException("Unknown type of $javaClass")
    }
}

internal fun SearchResult.fixNonDeterminedFields(userRecordPriority: Int, sessionID: String): SearchResult {
    this as BaseSearchResult
    val fixedOriginalSearchResult = originalSearchResult.copy(userRecordPriority = userRecordPriority)
    val fixedRequestOptions = requestOptions.copy(sessionID = sessionID)
    return when (this) {
        is ServerSearchResultImpl -> {
            copy(originalSearchResult = fixedOriginalSearchResult, requestOptions = fixedRequestOptions)
        }
        is IndexableRecordSearchResultImpl -> {
            copy(originalSearchResult = fixedOriginalSearchResult, requestOptions = fixedRequestOptions)
        }
        else -> throw IllegalStateException("Unknown type of $javaClass")
    }
}

internal fun compareSearchResultWithServerSearchResult(
    expected: SearchResult,
    serverResult: SearchResult
): Boolean {
    if (expected === serverResult) return true
    if (expected.javaClass != serverResult.javaClass) return false

    expected as BaseSearchResult
    serverResult as BaseSearchResult

    val fixedResult = expected.fixNonDeterminedFields(
        serverResult.originalSearchResult.userRecordPriority,
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

    expected as BaseSearchSuggestion
    serverResult as BaseSearchSuggestion

    val fixedResult = expected.fixNonDeterminedFields(
        serverResult.originalSearchResult.userRecordPriority,
        serverResult.requestOptions.sessionID
    )
    return fixedResult == serverResult
}

internal fun compareSearchResultWithServerSearchResult(
    expected: OriginalSearchResult,
    serverResult: OriginalSearchResult
): Boolean {
    if (expected === serverResult) return true
    if (expected.javaClass != serverResult.javaClass) return false

    val fixedOriginalSearchResult = expected.copy(
        userRecordPriority = serverResult.userRecordPriority
    )

    return fixedOriginalSearchResult == serverResult
}
