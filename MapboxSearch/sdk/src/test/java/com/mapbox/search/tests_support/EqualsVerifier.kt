package com.mapbox.search.tests_support

import com.mapbox.search.result.OriginalSearchResult
import nl.jqno.equalsverifier.api.SingleTypeEqualsVerifierApi

internal fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestOriginalSearchResult(
    red: OriginalSearchResult = createTestOriginalSearchResult(id = "test-result-1"),
    blue: OriginalSearchResult = createTestOriginalSearchResult(id = "test-result-2")
): SingleTypeEqualsVerifierApi<T> {
    return withPrefabValues(OriginalSearchResult::class.java, red, blue)
}
