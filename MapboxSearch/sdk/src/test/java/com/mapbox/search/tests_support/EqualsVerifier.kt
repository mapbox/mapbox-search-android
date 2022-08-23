package com.mapbox.search.tests_support

import com.mapbox.search.base.result.BaseRawSearchResult
import nl.jqno.equalsverifier.api.SingleTypeEqualsVerifierApi

internal fun <T> SingleTypeEqualsVerifierApi<T>.withPrefabTestBaseRawSearchResult(
    red: BaseRawSearchResult = createTestBaseRawSearchResult(id = "test-result-1"),
    blue: BaseRawSearchResult = createTestBaseRawSearchResult(id = "test-result-2")
): SingleTypeEqualsVerifierApi<T> {
    return withPrefabValues(BaseRawSearchResult::class.java, red, blue)
}
